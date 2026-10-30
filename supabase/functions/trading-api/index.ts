import { createHandler } from './core.js';
Deno.serve(createHandler(Deno.env.toObject()));
