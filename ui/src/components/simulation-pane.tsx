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
  variables: string[]
  setBackgroundJobInfo(running:boolean, msg?: string):Action
  addSimulationData(data:SimulationData):void
  notifyError(msg:string): void
}

class SimulationPaneCon extends React.Component<Props, any> {
  constructor(p: Props) {
    super(p)
    this.state = { data: undefined }
  }

  private simulate(sr: SimulateRequest): void {
    console.log("gonna simulate: ", sr)
    this.props.setBackgroundJobInfo(true, "simulating, please be patient.")
    this.props.api.simulate(sr)
      .then(l => {
        //this.props.addSimulationData({ address: url })
        this.queryResults(l)
      })
  }

  private queryResults(relativeLocation: string): void {
    console.log("query results ...")
    this.props.api
      .getSimulationResults(relativeLocation, 'chartjs', this.props.variables)
      .then(rs => {
        //TODO: handle multiple simulation results
        //save the data into local state and not redux store to avoid overflowing browser's storage size limits
        this.setState({ data: { address: relativeLocation, data:rs as TableFormat}})
        this.props.setBackgroundJobInfo(false)
      })
      .catch((er: ApiError) => {
        console.error("query results got error: ", er)
        if(er.isBadRequest()) {
          this.props.notifyError(er.statusText)
          this.props.setBackgroundJobInfo(false)
        } else if(er.code === 409) {
          window.setTimeout(() => this.queryResults(relativeLocation), 5000)
        } else {
          console.error("don't know how to handle error:", er)
          this.props.setBackgroundJobInfo(false)
        }
      })
  }

  render() {
    return (
      <WmContainer title={"Session: " + this.props.session.project.name} active="simulate" sessionId={this.props.session.id}>
        <Alert variant="secondary" dismissible>Before simulating, compile your model.</Alert>
        <SimulationSetup api={this.props.api} simulate={this.simulate.bind(this)} />

        {this.state.data && (<SimulationPlot data={this.state.data.data!} address={this.state.data.address} api={this.props.api} />)}
      </WmContainer>
    )
  }
}

function mapProps(state: AppState) {
  const results = state.session!.simulation.data
  return { session: state.session!, simulationData: (results.length>0) ? results[0] : undefined, variables: state.session!.simulation.variables }
}

function dispatchToProps(dispatch: (a: Action) => any) {
  return bindActionCreators({ addSimulationData, notifyError, setBackgroundJobInfo}, dispatch)
}

const SimulationPane = connect(mapProps, dispatchToProps)(SimulationPaneCon)
export default SimulationPane
