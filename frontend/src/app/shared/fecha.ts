// Formatea una fecha como YYYY-MM-DD usando el calendario LOCAL, no UTC
// (Date.toISOString() convierte a UTC y puede dar el día equivocado cerca de medianoche)
export function fechaLocalIso(fecha: Date = new Date()): string {
  const y = fecha.getFullYear();
  const m = String(fecha.getMonth() + 1).padStart(2, '0');
  const d = String(fecha.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}
