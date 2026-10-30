-- Additive workspace schema; existing auth tables and policies are preserved.
create table public.workspaces(id uuid primary key default gen_random_uuid(), access_key_hash text unique not null, enabled boolean not null default true);
alter table public.workspaces enable row level security;
revoke all on public.workspaces from anon, authenticated;
grant all on public.workspaces to service_role;
-- Apply to the dedicated V3.5 project. Writes are restricted to the backend.
create table public.workspace_bridge_connections (
  user_id uuid primary key references public.workspaces(id) on delete cascade,
  token_hash text unique not null,
  account text, server text, account_mode text,
  telemetry jsonb, last_seen timestamptz,
  created_at timestamptz not null default now()
);
create table public.workspace_market_ticks (
  user_id uuid not null references public.workspaces(id) on delete cascade,
  symbol text not null,
  payload jsonb not null,
  updated_at timestamptz not null default now(),
  primary key(user_id,symbol)
);
create table public.workspace_analyses (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.workspaces(id) on delete cascade,
  symbol text not null,
  payload jsonb not null,
  created_at timestamptz not null default now()
);
create index workspace_analyses_owner_created on public.workspace_analyses(user_id,created_at desc);
create table public.workspace_orders (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references public.workspaces(id) on delete cascade,
  analysis_id uuid not null references public.workspace_analyses(id),
  idempotency_key uuid not null,
  payload jsonb not null,
  status text not null default 'queued' check(status in ('queued','claimed','filled','partially_filled','rejected','expired','unknown')),
  broker_result jsonb,
  created_at timestamptz not null default now(),
  expires_at timestamptz not null default now() + interval '30 seconds',
  claimed_at timestamptz,
  unique(user_id,idempotency_key), unique(user_id,analysis_id)
);
create index workspace_orders_owner_created on public.workspace_orders(user_id,created_at desc);
create index workspace_orders_analysis_id on public.workspace_orders(analysis_id);
create index workspace_orders_queued on public.workspace_orders(user_id,created_at) where status='queued';
create table public.workspace_request_limits (user_id uuid primary key references public.workspaces(id) on delete cascade, next_allowed timestamptz not null);
alter table public.workspace_bridge_connections enable row level security;
alter table public.workspace_market_ticks enable row level security;
alter table public.workspace_analyses enable row level security;
alter table public.workspace_orders enable row level security;
alter table public.workspace_request_limits enable row level security;
revoke all on public.workspace_bridge_connections, public.workspace_market_ticks, public.workspace_analyses, public.workspace_orders, public.workspace_request_limits from anon, authenticated;
grant all on public.workspace_bridge_connections, public.workspace_market_ticks, public.workspace_analyses, public.workspace_orders, public.workspace_request_limits to service_role;
create function public.claim_workspace_order(owner_id uuid) returns setof public.workspace_orders
language plpgsql security invoker set search_path='' as $$
begin
  update public.workspace_orders set status='expired' where user_id=owner_id and status='queued' and expires_at<=now();
  -- A lost acknowledgement is uncertain and is never automatically requeued.
  update public.workspace_orders set status='unknown' where user_id=owner_id and status='claimed' and claimed_at < now()-interval '2 minutes';
  return query update public.workspace_orders set status='claimed', claimed_at=now()
  where id=(select id from public.workspace_orders where user_id=owner_id and status='queued' and expires_at>now() order by created_at for update skip locked limit 1)
  returning *;
end $$;
create function public.reserve_workspace_analysis(owner_id uuid) returns boolean
language plpgsql security invoker set search_path='' as $$
declare reserved uuid;
begin
  insert into public.workspace_request_limits(user_id,next_allowed) values(owner_id,now()+interval '60 seconds')
  on conflict(user_id) do update set next_allowed=excluded.next_allowed where public.workspace_request_limits.next_allowed<=now()
  returning user_id into reserved;
  return reserved is not null;
end $$;
revoke all on function public.claim_workspace_order(uuid), public.reserve_workspace_analysis(uuid) from public, anon, authenticated;
grant execute on function public.claim_workspace_order(uuid), public.reserve_workspace_analysis(uuid) to service_role;