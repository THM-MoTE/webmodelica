import { initialState, AppState, Session, SimulationOption, SimulationData, ProjectPreviewState } from '../models/state'
import { Project } from '../models/project'
import { File } from '../models/file'
import { Action, ActionTypes } from './actions'
import * as R from 'ramda'
import { CompilerError } from '../models';

const reducerMap = {
  [ActionTypes.SetProjects.toString()]: (state: AppState, data: Project[]) => { return { ...state, projects: data } },
  [ActionTypes.SetProject.toString()]: (state: AppState, project: Project) => ({ ...state,
    projects: R.reduce((acc:Project[], p) => R.append((p.id===project.id) ? project : p, acc), [], state.projects)
  }),
  [ActionTypes.UpdateSessionFiles.toString()]: function(state: AppState, data: File[]): AppState {
    if (state.session) {
      let pathNames = data.map(f => f.relativePath)
      //TODO: only update the files, that are new.. don't replace all files
      let oldFiles = R.filter((f: File) => !R.contains(f.relativePath, pathNames), state.session.files)
      let newFiles = R.sortBy((f: File) => f.relativePath, oldFiles.concat(data))
      return { ...state, session: { ...state!.session, files: newFiles } }
    } else {
      console.error("can't set session files if no session provided before!")
      return state
    }
  },
  [ActionTypes.SetSessionFiles.toString()]: (state: AppState, files: File[]) => ({...state, session: {...state.session!, files: R.sortBy(f => f.relativePath, files)}}),
  [ActionTypes.AddProject.toString()]: (state: AppState, data: Project) => ({ ...state, projects: R.prepend(data, state.projects) }),
  [ActionTypes.SetProjectPreview.toString()]: (state:AppState, data:ProjectPreviewState) => ({...state, projectPreview: data }),
  [ActionTypes.SetSession.toString()]: (state: AppState, session: Session) => ({ ...state, session: session }),
  [ActionTypes.UpdateWsToken.toString()]: (state: AppState, token: string) => {
    let payload = JSON.parse(atob(token.split('.')[1]))
    return {
      ...state,
      authentication: {
        username: payload.username,
        token: {
          username: payload.username,
          //iat & exp are in seconds, JS-Dates take miliseconds => *1000
          issued: new Date(payload.iat * 1000),
          expires: new Date(payload.exp * 1000),
          raw: token
        }
      }
    }
  },
  [ActionTypes.CreateNewFile.toString()]: (state: AppState, f: File) => ({ ...state, session: { ...state.session!, files: R.append(f, state.session!.files) } }),
  [ActionTypes.SetCompilerErrors.toString()]: (state: AppState, errors: CompilerError[]) => ({ ...state, session: { ...state.session!, compilerErrors: errors}}),
  [ActionTypes.UpdateSimulationOption.toString()]: (state: AppState, payload: any) => {
    const { idx, option } = payload
    const simulationOptions:SimulationOption[] = state.session!.simulation.options.map((opt, i) => {
      if(i === idx) return option
      else return opt
    })
    return R.assocPath(["session", "simulation", "options"], simulationOptions, state) as AppState
  },
  [ActionTypes.AddSimulationOption.toString()]: (state: AppState, payload:SimulationOption) =>
    R.assocPath(["session", "simulation", "options"], R.append(payload, state.session!.simulation.options), state) as AppState,
  [ActionTypes.DeleteSimulationOption.toString()]: (state: AppState, idx: number) =>
    R.assocPath(["session", "simulation", "options"], state.session!.simulation.options.filter((_,i) => i !== idx), state) as AppState,
  [ActionTypes.ParseSimulationOptions.toString()]: (state:AppState, options:SimulationOption[]) => {
    const opts:SimulationOption[] = options.map(o => {
        //convert string to number if it's a true float number
        //parseFloat returns NaN if it couldn't convert to float
        const f: number = parseFloat(o.value as any)
        const v: string | number = (!isNaN(f)) ? f : o.value
        return ({ name: o.name, value: v })
      })
    return R.assocPath(["session", "simulation", "options"], opts, state)
  },
  [ActionTypes.AddSimulationData.toString()]: (state: AppState, data:SimulationData) =>
    R.assocPath(["session", "simulation", "data"], [data], state)
}

export function rootReducer(state: AppState = initialState(), action: Action): AppState {
  const fn = reducerMap[action.type.toString()]
  if (fn) {
    let st = fn(state, action.payload)
    return st
  } else {
    console.error("no reducer applied to", action)
    return state
  }
}
