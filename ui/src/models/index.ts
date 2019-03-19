export * from './file'
export * from './project'
export * from './token'
export * from './state'
export * from './modelica-syntax'
export * from './compiler-error'

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
