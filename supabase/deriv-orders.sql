create table public.deriv_orders (
 id uuid primary key default gen_random_uuid(),
 user_id uuid not null references public.workspaces(id),
 analysis_id uuid not null references public.workspace_analyses(id),
 payload jsonb not null,
 status text not null default 'quoted' check(status in ('quoted','submitting','purchased','rejected','unknown','expired')),
 broker_result jsonb,
 created_at timestamptz not null default now(),
 expires_at timestamptz not null default now()+interval '30 seconds',
 submitted_at timestamptz,
 unique(user_id,analysis_id)
);
create index deriv_orders_analysis on public.deriv_orders(analysis_id);
create index deriv_orders_owner_created on public.deriv_orders(user_id,created_at desc);
alter table public.deriv_orders enable row level security;
revoke all on public.deriv_orders from anon,authenticated;
grant all on public.deriv_orders to service_role;
