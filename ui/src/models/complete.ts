
export interface Position {
  column: number
  line: number
}

export interface Complete {
  file: string
  message: string
  start: Position
  end: Position
  type: string
}
