import { initialState, AppState, Session, SimulationOption, SimulationData, ProjectPreviewState, Notification, BackgroundJobInfo } from '../models/state'
import { Project } from '../models/project'
import { File, FileNode, setId, toggleRoot } from '../models/file'
import { Action, ActionTypes } from './actions'
import * as utils from '../utils'
import * as R from 'ramda'
import { CompilerError, AuthServiceToken, WebmodelicaToken } from '../models/index';
import { oc } from 'ts-optchain';

const setupNodes = R.compose(toggleRoot, setId)

const reducerMap = {
  [ActionTypes.SetProjects.toString()]: (state: AppState, data: Project[]) => { return { ...state, projects: data } },
  [ActionTypes.SetProject.toString()]: (state: AppState, project: Project) => ({
    ...state,
    projects: R.reduce((acc: Project[], p) => R.append((p.id === project.id) ? project : p, acc), [], state.projects)
  }),
  [ActionTypes.SetSessionFiles.toString()]: (state: AppState, files: FileNode) => ({ ...state, session: { ...state.session!, files: setupNodes(files) } }),
  [ActionTypes.AddProject.toString()]: (state: AppState, data: Project) => ({ ...state, projects: R.prepend(data, state.projects) }),
  [ActionTypes.SetProjectPreview.toString()]: (state: AppState, data: ProjectPreviewState) => ({ ...state, projectPreview: { ...data, files: setupNodes(data.files) } }),
  [ActionTypes.SetOpenFile.toString()]: (state: AppState, file: File) => R.assocPath(['session', 'openedFile'], file, state),
  [ActionTypes.SetSession.toString()]: (state: AppState, session: Session) => ({ ...state, session: session }),
  [ActionTypes.UpdateWsToken.toString()]: (state: AppState, token: string) => {
    //if the token came from auth-service it has a different structure than our token
    let payload: AuthServiceToken | WebmodelicaToken = JSON.parse(atob(token.split('.')[1]))

    let obj = { ...state }
    if ('data' in payload) { //payload is a AuthServiceToken
      //if there is either a first_name, last_name or both use it as displayName. if not use the 'username'
      let firstName = payload.data.first_name || ""
      let lastName =  payload.data.last_name || ""
      let displayName = (firstName+" "+lastName).trim()
      displayName = (R.isEmpty(displayName)) ? payload.data.username : displayName
      obj.authentication = {
        username: payload.data.username,
        displayName: displayName,
        token: {
          username: payload.data.username,
          //exp are in seconds, JS-Dates take miliseconds => *1000
          expires: new Date(payload.exp * 1000),
          raw: token
        }
      }
    } else {
      obj.authentication = { //payload is WebmodelicaToken
        username: payload.username,
        displayName: payload.username,
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
  [ActionTypes.SetCompilerErrors.toString()]: (state: AppState, errors: CompilerError[]) => ({ ...state, session: { ...state.session!, compilerErrors: errors } }),
  [ActionTypes.UpdateSimulationOption.toString()]: (state: AppState, payload: any) => {
    const { idx, option } = payload
    const simulationOptions: SimulationOption[] = state.session!.simulation.options.map((opt, i) => {
      if (i === idx) {
        return option
      }
      else return opt
    })
    return R.assocPath(["session", "simulation", "options"], simulationOptions, state) as AppState
  },
  [ActionTypes.AddSimulationOption.toString()]: (state: AppState, payload: SimulationOption) =>
    R.assocPath(["session", "simulation", "options"], R.append(payload, state.session!.simulation.options), state) as AppState,
  [ActionTypes.DeleteSimulationOption.toString()]: (state: AppState, idx: number) =>
    R.assocPath(["session", "simulation", "options"], state.session!.simulation.options.filter((_, i) => i !== idx), state) as AppState,
  [ActionTypes.ParseSimulationOptions.toString()]: (state: AppState, options: SimulationOption[]) => {
    return R.assocPath(["session", "simulation", "options"], options, state)
  },
  [ActionTypes.AddSimulationData.toString()]: (state: AppState, data: SimulationData) =>
    R.assocPath(["session", "simulation", "data"], [data], state),
  [ActionTypes.SetSimulationVariables.toString()]: (state: AppState, variables: string[]) =>
    R.assocPath(["session", "simulation", "variables"], variables, state),
  [ActionTypes.NewNotification.toString()]: (state: AppState, data: Notification) =>
    R.assoc("notifications", R.append(data, state.notifications), state),
  [ActionTypes.RemoveNotifications.toString()]: (state: AppState, data: Notification[]) =>
    R.assoc("notifications", state.notifications.filter(n => !R.contains(n, data)), state),
  [ActionTypes.SetBackgroundJobInfo.toString()]: (state: AppState, info: BackgroundJobInfo) =>
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
