
import { CompilerError, File, FilePath, FileNode, Project, TableFormat, UserAuth } from './index'
import {isFuture} from 'date-fns'

export interface Session {
  project: Project //the underlying project
  id: string
  files: FileNode, //filetree for FileTree component
  openedFile?: File //file that is open in monaco editor
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
  variables: string[]
}

export interface SimulationOption {
  name: string
  value: number | string
}

export interface ProjectPreviewState {
  project: Project //project to preview
  files: FileNode //filetree for FileTree component
}

//indicates that a background job (e.g.: ajax call) is running and that the
//loading overlay should be displayed
export interface BackgroundJobInfo {
  running: boolean
  message?: string
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

/** Main applicatino state that is stored inside of redux. */
export interface AppState {
  authentication?: UserAuth //authentication informations
  projects: Project[] //all available projects
  projectPreview?: ProjectPreviewState //state for project preview
  session?: Session //currently opened session
  notifications: Notification[] //all notifications, displayed in the nofitication area of WmContainer
  jobInfo: BackgroundJobInfo
}

export function initialState(): AppState {
  return {
    authentication: undefined,
    projects: [],
    session: undefined,
    notifications: [],
    jobInfo: {running: false}
  }
}
export function userIsAuthenticated(auth?:UserAuth):boolean {
  return (auth) ? isFuture(auth!.token.expires) : false
}
