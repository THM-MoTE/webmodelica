import * as R from 'ramda'

export interface SimulationResult {
  modelName: string
  variables: { [name: string]: number[] }
}
export interface SimulateRequest {
  modelName: string
  options: { [key: string]: string | number }
}

export interface TableFormat {
  modelName: string,
  data: number[][],
  header: string[],
  dataManipulated?:string
}

export interface SimulationOptionSuggestions {
  key: string,
  values?: string[]
}

function simSugestion(k: string, ...values: string[]): SimulationOptionSuggestions {
  return {
    key: k,
    values: (R.isEmpty(values)) ? undefined : values
  }
}

export function simulationValuesFor(name:string): string[] {
  const option = availableSimulationOptions.find(opt => opt.key===name)
  return (option) ? option.values || [] : []
}

export const availableSimulationOptions: SimulationOptionSuggestions[] = [
  simSugestion("startTime"),
  simSugestion("stopTime"),
  simSugestion("stepSize"),
  simSugestion("numberOfIntervals"),
  simSugestion("method", "dassl", "euler", "ida", "heun"),
  simSugestion("tolerance")
]
