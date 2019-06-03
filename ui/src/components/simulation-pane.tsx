import React from 'react';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { WmContainer } from '../partials/container'
import SimulationPlot from '../partials/simulation-plot'
import SimulationSetup from '../partials/simulation-setup'
//@ts-ignore
import Octicon from 'react-octicon'
import { Row, Col, Button, ButtonGroup, Container as RContainer, Card, Alert } from 'react-bootstrap'
import { renderErrors } from '../partials/errors';
import * as R from 'ramda';
import { Action, addSimulationData, notifyError, setBackgroundJobInfo } from '../redux/actions'
import { AppState, Session, SimulationResult, TableFormat, SimulateRequest, SimulationState, ApiError } from '../models/index'
import { ApiClient } from '../services/api-client'
import { LoadingSpinner } from '../partials/loading-spinner';
import { SimulationData } from '../models';

interface Props {
  api: ApiClient
  session: Session
  simulationData?: SimulationData
  setBackgroundJobInfo(running:boolean, msg?: string):Action
  addSimulationData(data:SimulationData):void
  notifyError(msg:string): void
}

interface State {
  simulating:boolean
}

class SimulationPaneCon extends React.Component<Props, State> {
  constructor(p: Props) {
    super(p)
    this.state = {simulating: false}
  }

  private simulate(sr: SimulateRequest): void {
    console.log("gonna simulate: ", sr)
    this.setState({ simulating: true })
    this.props.setBackgroundJobInfo(true, "simulating be patient..")
    this.props.api.simulate(sr)
      .then(l => {
        const url = new URL(l)
        url.host = window.location.host
        this.props.addSimulationData({ address: url })
        this.queryResults(url)
      })
  }

  private queryResults(location: URL): void {
    console.log("query results ...")
    this.props.api
      .getSimulationResults(location)
      .then(rs => {
        //TODO: handle multiple simulation results
        this.props.addSimulationData({address: location, data:rs as TableFormat})
        this.setState({simulating: false})
        this.props.setBackgroundJobInfo(false)
      })
      .catch((er: ApiError) => {
        if(er.isBadRequest()) {
          this.props.notifyError(er.statusText)
          this.setState({simulating: false})
          this.props.setBackgroundJobInfo(false)
        } else if(er.code === 409) {
          window.setTimeout(() => this.queryResults(location), 5000)
        } else {
          console.error("don't know how to handle error:", er)
          this.props.setBackgroundJobInfo(false)
        }
      })
  }

  render() {
    return (
      <WmContainer title={"Session: " + this.props.session.project.name} active="simulation" sessionId={this.props.session.id}>
        <Alert variant="secondary" dismissible>Before simulating, compile your model.</Alert>
        <SimulationSetup api={this.props.api} simulate={this.simulate.bind(this)} />

        {this.props.simulationData && this.props.simulationData.data && (<SimulationPlot data={this.props.simulationData.data!} address={this.props.simulationData.address} api={this.props.api} />)}
      </WmContainer>
    )
  }
}

function mapProps(state: AppState) {
  const results = state.session!.simulation.data
  return { session: state.session!, simulationData: (results.length>0) ? results[0] : undefined }
}

function dispatchToProps(dispatch: (a: Action) => any) {
  return bindActionCreators({ addSimulationData, notifyError, setBackgroundJobInfo}, dispatch)
}

const SimulationPane = connect(mapProps, dispatchToProps)(SimulationPaneCon)
export default SimulationPane
