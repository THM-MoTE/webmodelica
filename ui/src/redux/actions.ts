import { UserAuth, Project, File, Session, CompilerError, SimulationOption, SimulationData } from '../models/index'

export enum ActionTypes {
  Logout,
  SetProjects,
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
  AddSimulationData
}

export interface Action {
  type: ActionTypes,
  payload?: any
}

export const updateToken: (pl: String) => Action = pl => ({ type: ActionTypes.UpdateWsToken, payload: pl })
export const addProject = (p: Project) => ({ type: ActionTypes.AddProject, payload: p })
export const setProjects: (ps: Project[]) => Action = (ps: Project[]) => { return { type: ActionTypes.SetProjects, payload: ps } }
export const updateSessionFiles: (fs: File[]) => Action = (fs: File[]) => ({ type: ActionTypes.UpdateSessionFiles, payload: fs })
export const setSession = (s: Session) => ({ type: ActionTypes.SetSession, payload: s })
export const setCompilerErrors = (ers: CompilerError[]) => ({ type: ActionTypes.SetCompilerErrors, payload: ers})
export const newFile = (f: File) => ({ type: ActionTypes.CreateNewFile, payload: f })
export const addOption = (o: SimulationOption) => ({ type: ActionTypes.AddSimulationOption, payload: o})
export const updateOption = (idx: number, o: SimulationOption) => ({ type: ActionTypes.UpdateSimulationOption, payload: {idx: idx, option: o}})
export const deleteOption = (idx: number) => ({type: ActionTypes.DeleteSimulationOption, payload: idx})
export const setSessionFiles = (files:File[]) => ({type: ActionTypes.SetSessionFiles, payload: files})
export const addSimulationData = (data: SimulationData) => ({type: ActionTypes.AddSimulationData, payload: data})
