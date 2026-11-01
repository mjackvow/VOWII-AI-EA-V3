import { createHandler } from './core.js';
// Explicit temporary production selection; never silently drop failed votes.
const env = Deno.env.toObject();
Deno.serve(createHandler({...env, AI_MODELS: env.AI_ACTIVE_MODELS || 'nvidia/nemotron-3.5-bolt-e4b(Nvidia)'}));
