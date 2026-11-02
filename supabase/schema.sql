-- Apply to the dedicated V3.5 project. Writes are restricted to the backend.
create table public.bridge_connections (
  user_id uuid primary key references auth.users(id) on delete cascade,
  token_hash text unique not null,
  account text, server text, account_mode text,
  telemetry jsonb, last_seen timestamptz,
  created_at timestamptz not null default now()
);
create table public.market_ticks (
  user_id uuid not null references auth.users(id) on delete cascade,
  symbol text not null,
  payload jsonb not null,
  updated_at timestamptz not null default now(),
  primary key(user_id,symbol)
);
create table public.analyses (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  symbol text not null,
  payload jsonb not null,
  created_at timestamptz not null default now()
);
create index analyses_owner_created on public.analyses(user_id,created_at desc);
create table public.orders (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  analysis_id uuid not null references public.analyses(id),
  idempotency_key uuid not null,
  payload jsonb not null,
  status text not null default 'queued' check(status in ('queued','claimed','filled','partially_filled','rejected','expired','unknown')),
  broker_result jsonb,
  created_at timestamptz not null default now(),
  expires_at timestamptz not null default now() + interval '30 seconds',
  claimed_at timestamptz,
  unique(user_id,idempotency_key), unique(user_id,analysis_id)
);
create index orders_owner_created on public.orders(user_id,created_at desc);
create index orders_analysis_id on public.orders(analysis_id);
create index orders_queued on public.orders(user_id,created_at) where status='queued';
create table public.request_limits (user_id uuid primary key references auth.users(id) on delete cascade, next_allowed timestamptz not null);
create table public.release_files(version text not null,path text not null,content text not null,sha256 text not null,stored_at timestamptz not null default now(),primary key(version,path));
alter table public.release_files enable row level security;
revoke all on public.release_files from anon,authenticated;
grant all on public.release_files to service_role;
alter table public.bridge_connections enable row level security;
alter table public.market_ticks enable row level security;
alter table public.analyses enable row level security;
alter table public.orders enable row level security;
alter table public.request_limits enable row level security;
revoke all on public.bridge_connections, public.market_ticks, public.analyses, public.orders, public.request_limits from anon, authenticated;
grant select on public.market_ticks, public.analyses, public.orders to authenticated;
grant all on public.bridge_connections, public.market_ticks, public.analyses, public.orders, public.request_limits to service_role;
create policy own_ticks on public.market_ticks for select to authenticated using ((select auth.uid())=user_id);
create policy own_analyses on public.analyses for select to authenticated using ((select auth.uid())=user_id);
create policy own_orders on public.orders for select to authenticated using ((select auth.uid())=user_id);
create function public.claim_order(owner_id uuid) returns setof public.orders
language plpgsql security invoker set search_path='' as $$
begin
  update public.orders set status='expired' where user_id=owner_id and status='queued' and expires_at<=now();
  -- A lost acknowledgement is uncertain and is never automatically requeued.
  update public.orders set status='unknown' where user_id=owner_id and status='claimed' and claimed_at < now()-interval '2 minutes';
  return query update public.orders set status='claimed', claimed_at=now()
  where id=(select id from public.orders where user_id=owner_id and status='queued' and expires_at>now() order by created_at for update skip locked limit 1)
  returning *;
end $$;
create function public.reserve_analysis(owner_id uuid) returns boolean
language plpgsql security invoker set search_path='' as $$
declare reserved uuid;
begin
  insert into public.request_limits(user_id,next_allowed) values(owner_id,now()+interval '60 seconds')
  on conflict(user_id) do update set next_allowed=excluded.next_allowed where public.request_limits.next_allowed<=now()
  returning user_id into reserved;
  return reserved is not null;
end $$;
revoke all on function public.claim_order(uuid), public.reserve_analysis(uuid) from public, anon, authenticated;
grant execute on function public.claim_order(uuid), public.reserve_analysis(uuid) to service_role;
insert into storage.buckets(id,name,public,file_size_limit) values('app-files','app-files',false,10485760),('release-artifacts','release-artifacts',false,52428800);
create policy own_files_read on storage.objects for select to authenticated using(bucket_id='app-files' and (storage.foldername(name))[1]=(select auth.uid())::text);
create policy own_files_insert on storage.objects for insert to authenticated with check(bucket_id='app-files' and (storage.foldername(name))[1]=(select auth.uid())::text);
create policy own_files_update on storage.objects for update to authenticated using(bucket_id='app-files' and (storage.foldername(name))[1]=(select auth.uid())::text) with check(bucket_id='app-files' and (storage.foldername(name))[1]=(select auth.uid())::text);
create policy own_files_delete on storage.objects for delete to authenticated using(bucket_id='app-files' and (storage.foldername(name))[1]=(select auth.uid())::text);
