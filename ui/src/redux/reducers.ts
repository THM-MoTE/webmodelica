import { initialState, AppState, Session, SimulationOption, SimulationData, ProjectPreviewState, Notification, BackgroundJobInfo } from '../models/state'
import { Project } from '../models/project'
import { File, FileNode, setId } from '../models/file'
import { Action, ActionTypes } from './actions'
import * as R from 'ramda'
import { CompilerError, AuthServiceToken, WebmodelicaToken } from '../models';

const reducerMap = {
  [ActionTypes.SetProjects.toString()]: (state: AppState, data: Project[]) => { return { ...state, projects: data } },
  [ActionTypes.SetProject.toString()]: (state: AppState, project: Project) => ({ ...state,
    projects: R.reduce((acc:Project[], p) => R.append((p.id===project.id) ? project : p, acc), [], state.projects)
  }),
  [ActionTypes.SetSessionFiles.toString()]: (state: AppState, files: FileNode) => ({...state, session: {...state.session!, files: setId(files)}}),
  [ActionTypes.AddProject.toString()]: (state: AppState, data: Project) => ({ ...state, projects: R.prepend(data, state.projects) }),
  [ActionTypes.SetProjectPreview.toString()]: (state:AppState, data:ProjectPreviewState) => ({...state, projectPreview: data }),
  [ActionTypes.SetSession.toString()]: (state: AppState, session: Session) => ({ ...state, session: session }),
  [ActionTypes.UpdateWsToken.toString()]: (state: AppState, token: string) => {
    //if the token came from auth-service it has a different structure than our token
    let payload: AuthServiceToken | WebmodelicaToken = JSON.parse(atob(token.split('.')[1]))
    let obj = {...state}
    if('user' in payload) { //payload is a AuthServiceToken
      obj.authentication = {
        username: payload.user.username,
        token: {
          username: payload.user.username,
          //exp are in seconds, JS-Dates take miliseconds => *1000
          expires: new Date(payload.exp*1000),
          raw: token
        }
      }
    } else {
      obj.authentication = { //payload is WebmodelicaToken
          username: payload.username,
          token: {
            username: payload.username,
            //exp are in seconds, JS-Dates take miliseconds => *1000
            expires: new Date(payload.exp * 1000),
            raw: token
          }
      }
    }
    return obj
  },
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
    R.assocPath(["session", "simulation", "data"], [data], state),
  [ActionTypes.NewNotification.toString()]: (state: AppState, data: Notification) =>
    R.assoc("notifications", R.append(data, state.notifications), state),
  [ActionTypes.RemoveNotifications.toString()]: (state: AppState, data: Notification[]) =>
    R.assoc("notifications", state.notifications.filter(n => !R.contains(n, data)), state),
  [ActionTypes.SetBackgroundJobInfo.toString()]: (state: AppState, info:BackgroundJobInfo) =>
    R.assoc('jobInfo', info, state),
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
