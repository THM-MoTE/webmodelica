import React from 'react';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { WmContainer } from '../partials/container'
import SimulationPlot from '../partials/simulation-plot'
import SimulationSetup from '../partials/simulation-setup'
//@ts-ignore
import Octicon from 'react-octicon'
import { Row, Col, Button, ButtonGroup, Container as RContainer, Card } from 'react-bootstrap'
import { renderErrors } from '../partials/errors';
import * as R from 'ramda';
import { Action } from '../redux/actions'
import { AppState, Session, SimulationResult, TableFormat, SimulateRequest } from '../models/index'
import { ApiClient } from '../services/api-client'
import { LoadingSpinner } from '../partials/loading-spinner';

interface Props {
  api: ApiClient
  session: Session
}

interface State {
  resultSet?: TableFormat
  resultLocation?: URL
  simulating:boolean
}

class SimulationPaneCon extends React.Component<Props, State> {
  constructor(p: Props) {
    super(p)
    this.state = {simulating: false}
  }

  componentDidMount() {
    // this.props.api
    //   .getSimulationResults("/simulation-example.json")
    //   .then(rs => this.setState({
    //     resultSet: (rs as TableFormat)
    //   }))
  }

  private simulate(sr: SimulateRequest): void {
    console.log("gonna simulate: ", sr)
    this.setState({ resultSet: undefined, resultLocation: undefined, simulating: true })
    this.props.api.simulate(sr)
      .then(l => {
        const url = new URL(l)
        url.host = window.location.host
        this.setState({ resultLocation: url })
        this.queryResults(url)
      })
  }

  private queryResults(location: URL): void {
    console.log("query results ...")
    this.props.api
      .getSimulationResults(location)
      .then(rs => this.setState({ resultSet: (rs as TableFormat) }))
      .catch(er => window.setTimeout(() => this.queryResults(location), 5000))
  }

  render() {
    return (
      <WmContainer title={"Session: " + this.props.session.project.name} active="simulation" sessionId={this.props.session.id}>

        <SimulationSetup api={this.props.api} simulate={this.simulate.bind(this)} />

        {this.state.resultSet && (<SimulationPlot data={this.state.resultSet} api={this.props.api} />)}
        <LoadingSpinner msg={"simulating be patient.."} display={this.state.resultSet === undefined && this.state.simulating}/>
      </WmContainer>
    )
  }
}

function mapProps(state: AppState) {
  return { session: state.session! }
}

function dispatchToProps(dispatch: (a: Action) => any) {
  return bindActionCreators({}, dispatch)
}

const SimulationPane = connect(mapProps, dispatchToProps)(SimulationPaneCon)
export default SimulationPane
