
import { CompilerError, File, Project, TableFormat } from './index'
import {isFuture} from 'date-fns'

export interface UserAuth {
  username: string
  token: JwtToken
}

export interface JwtToken {
  username: string
  expires: Date
  raw: string
}

export interface Session {
  project: Project
  id: string
  files: File[],
  openedFiles: File[]
  compilerErrors: CompilerError[]
  simulation: SimulationState
}

export interface SimulationData {
  data?: TableFormat
  address: URL
}

export interface SimulationState {
  options: SimulationOption[]
  data: SimulationData[]
}

export interface SimulationOption {
  name: string
  value: number | string
}

export interface ProjectPreviewState {
  project: Project
  files: File[]
}

export enum NotificationType {
  Info,
  Warning,
  Error
}

export interface Notification {
  type: NotificationType
  message: string
}

export interface AppState {
  authentication?: UserAuth
  projects: Project[]
  projectPreview?: ProjectPreviewState
  session?: Session
  notifications: Notification[]
}

export function initialState(): AppState {
  return {
    authentication: undefined,
    projects: [],
    session: undefined,
    notifications: []
  }
}
export function userIsAuthenticated(auth?:UserAuth):boolean {
  return (auth) ? isFuture(auth!.token.expires) : false
}
