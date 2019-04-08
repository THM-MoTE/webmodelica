export * from './file'
export * from './project'
export * from './token'
export * from './state'
export * from './modelica-syntax'
export * from './compiler-error'
import R from 'ramda'

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
  header: string[]
}

export interface SimulationOptionSuggestions {
  key:string,
  values?: string[]
}

function simSugestion(k: string, ...values:string[]): SimulationOptionSuggestions {
  return {
    key: k,
    values: (R.isEmpty(values)) ? undefined : values
  }
}

export const availableSimulationOptions: SimulationOptionSuggestions[] = [
  simSugestion("startTime"),
  simSugestion("stopTime"),
  simSugestion("numberOfIntervals"),
  simSugestion("method", "dassl", "euler", "ida", "heun"),
  simSugestion("tolerance")
]
