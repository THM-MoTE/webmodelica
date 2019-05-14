import { UserAuth, Project, File, Session, CompilerError, SimulationOption, SimulationData, Notification, NotificationType } from '../models/index'

export enum ActionTypes {
  Logout,
  SetProjects,
  SetProject,
  AddProject,
  UpdateSessionFiles,
  SetSessionFiles,
  SetSession,
  UpdateWsToken,
  CreateNewFile,
  SetCompilerErrors,
  AddSimulationOption,
  UpdateSimulationOption,
  DeleteSimulationOption,
  ParseSimulationOptions,
  AddSimulationData,
  SetProjectPreview,
  NewNotification,
  RemoveNotifications
}

export interface Action {
  type: ActionTypes,
  payload?: any
}

export const updateToken: (pl: String) => Action = pl => ({ type: ActionTypes.UpdateWsToken, payload: pl })
export const addProject = (p: Project) => ({ type: ActionTypes.AddProject, payload: p })
export const setProjects: (ps: Project[]) => Action = (ps: Project[]) => { return { type: ActionTypes.SetProjects, payload: ps } }
export const setProject: (ps: Project) => Action = (ps: Project) => { return { type: ActionTypes.SetProject, payload: ps } }
export const updateSessionFiles: (fs: File[]) => Action = (fs: File[]) => ({ type: ActionTypes.UpdateSessionFiles, payload: fs })
export const setSession = (s: Session) => ({ type: ActionTypes.SetSession, payload: s })
export const setCompilerErrors = (ers: CompilerError[]) => ({ type: ActionTypes.SetCompilerErrors, payload: ers})
export const newFile = (f: File) => ({ type: ActionTypes.CreateNewFile, payload: f })
export const addOption = (o: SimulationOption) => ({ type: ActionTypes.AddSimulationOption, payload: o})
export const updateOption = (idx: number, o: SimulationOption) => ({ type: ActionTypes.UpdateSimulationOption, payload: {idx: idx, option: o}})
export const deleteOption = (idx: number) => ({type: ActionTypes.DeleteSimulationOption, payload: idx})
export const setSessionFiles = (files:File[]) => ({type: ActionTypes.SetSessionFiles, payload: files})
export const addSimulationData = (data: SimulationData) => ({type: ActionTypes.AddSimulationData, payload: data})
export const parseSimulationOptions = (pl: SimulationOption[]) => ({type: ActionTypes.ParseSimulationOptions, payload: pl})
export const setProjectPreview = (p: Project, files: []) => ({ type: ActionTypes.SetProjectPreview, payload: {project:p, files:files}})
export const addNotification = (n:Notification) => ({type: ActionTypes.NewNotification, payload: n})
export const notifyInfo = (msg:string) => addNotification({type: NotificationType.Info, message: msg})
export const notifyError = (msg:string) => addNotification({type: NotificationType.Error, message: msg})
export const notifyWarning = (msg:string) => addNotification({type: NotificationType.Warning, message: msg})
export const removeNotifications = (n: Notification[]) => ({ type: ActionTypes.RemoveNotifications, payload: n })
