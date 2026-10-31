-- Additive, private storage for actual per-model connection checks.
create table public.workspace_ai_checks (
 user_id uuid not null references public.workspaces(id) on delete cascade,
 provider text not null,
 model text not null,
 payload jsonb not null,
 checked_at timestamptz not null default now(),
 primary key(user_id,provider,model)
);
alter table public.workspace_ai_checks enable row level security;
revoke all on public.workspace_ai_checks from anon,authenticated;
grant all on public.workspace_ai_checks to service_role;
