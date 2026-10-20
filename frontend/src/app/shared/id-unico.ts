// Reemplaza crypto.randomUUID(): esa función solo existe en "contextos seguros" (HTTPS o localhost),
// así que se rompía al entrar por la IP de Tailscale en HTTP desde otra computadora o el celular.
// Este id es solo para distinguir renglones en el carrito del lado del cliente, no necesita ser criptográfico.
export function idUnico(): string {
  return `${Date.now().toString(36)}-${Math.random().toString(36).slice(2)}`;
}
