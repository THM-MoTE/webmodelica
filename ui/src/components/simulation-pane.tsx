import React from 'react';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { WmContainer } from '../partials/container'
import SimulationPlot from '../partials/simulation-plot'
//@ts-ignore
import Octicon from 'react-octicon'
import { Row, Col, Button, ButtonGroup, Container as RContainer, Card } from 'react-bootstrap'
import { renderErrors } from '../partials/errors';
import * as R from 'ramda';
import { Action } from '../redux/actions'
import { AppState, Session, SimulationResult, TableFormat } from '../models/index'
import { ApiClient } from '../services/api-client'

interface Props {
  api: ApiClient
  session: Session
}

interface State {
  resultSet?: TableFormat
}

class SimulationPaneCon extends React.Component<Props, State> {
  constructor(p: Props) {
    super(p)
    this.state = { resultSet: undefined }
  }

  componentDidMount() {
    this.props.api
      .getSimulationResults("/simulation-example.json")
      .then(rs => this.setState({
        resultSet: (rs as TableFormat)
      }))
  }

  render() {
    return (
      <WmContainer title={"Session: " + this.props.session.project.name} active="simulation" sessionId={this.props.session.id}>
        {this.state.resultSet && (<SimulationPlot data={this.state.resultSet} api={this.props.api} />)}
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
