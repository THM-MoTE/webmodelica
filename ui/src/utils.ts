export function capitalizeString(s:string): string {
  return s.charAt(0).toUpperCase() + s.slice(1)
}
export function downCaseString(s: string): string {
  return s.charAt(0).toLowerCase() + s.slice(1)
}
//convert string to number if it's a true float number
//parseFloat returns NaN if it couldn't convert to float
export function convertFloat(number:any): string|number {
  const f: number = parseFloat(number)
  const v: string | number = (!isNaN(f)) ? f : number
  return v
}
