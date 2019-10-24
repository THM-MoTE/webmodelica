import React from 'react';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { Row, Col, Button, Form, ButtonGroup } from 'react-bootstrap'
//@ts-ignore
import Octicon from 'react-octicon'
import { AppState, Session, File, TableFormat, SimulateRequest, SimulationOption, availableSimulationOptions } from '../models/index'
import { ApiClient } from '../services/api-client'
import { Action, parseSimulationOptions, setSimulationVariables, notifyInfo } from '../redux/actions'
import * as utils from '../utils'
import * as R from 'ramda';
import SimulationOptions from './simulation-options'
import { strictEqual } from 'assert';
import { EditorsPane } from '../components';

interface Props {
  api: ApiClient
  openFile?: File
  simulate(sr: SimulateRequest):void
  options: SimulationOption[]
  variables: string[]
  parseSimulationOptions(options:SimulationOption[]):void
  setSimulationVariables(variables: string[]): void
  notifyInfo(msg:string):void
}

interface State {
  validated?:boolean
  modelName: string
  rawVariableFilter: string
}

const withinPackagePattern = /within\s+([\w\.]+)/m //extracts 'within a.b.c.examples'
const modelNamePattern = /(?:(?:model)|(?:class))\s+(\w+)/m //extracts the model name: 'model FullModel'
const experimentPattern = /experiment\((.+)\)/m //extracts 'experiment([experimentOptions])'
const experimentOptionsPattern = /(\w+)\s*\=\s*([\w\.\-]+)/g //extracts 'startTime = 0' from result of experimentPattern

class SimulationSetupCon extends React.Component<Props, State> {

  constructor(p:Props) {
    super(p)
    this.state = { modelName: this.openedModelName() || '', rawVariableFilter: p.variables.join(' ')}
    this.experimentOptions()
  }

  private simulateClicked(ev:any) {
    const form = ev.currentTarget
    const validated = form.checkValidity()
    this.setState({validated})
    console.log("form target is: ", form, "form is valid: ", validated)
    if(validated) {
      const opts = R.fromPairs(
        this.props.options
          .filter(o => !R.empty(o.name.trim()))
          .map(o => R.pair(o.name, utils.convertFloat(o.value)))
        )
      this.props.simulate({modelName: this.state.modelName, options: opts})
    }
  }

  //greps the modelname inside of the currently open file
  private openedModelName(): string|undefined {
    if (this.props.openFile) {
      const content = this.props.openFile.content
      const withinMatch = withinPackagePattern.exec(content)
      const modelMatch = modelNamePattern.exec(content)
      const withinPrefix = (withinMatch) ? withinMatch[1]+'.' : ''
      const modelName = (modelMatch) ? withinPrefix+modelMatch[1] : ''
      return modelName
    } else {
      return undefined
    }
  }

  private experimentOptions() {
    if(this.props.openFile) {
      const content = this.props.openFile.content
      const experimentMatch = experimentPattern.exec(content)
      let optionMatch = (experimentMatch) ? experimentOptionsPattern.exec(experimentMatch[1]) : undefined
      let parsedOptions:SimulationOption[] = []
      while(optionMatch) {
        const name = utils.downCaseString(optionMatch[1])
        const value = optionMatch[2]
        parsedOptions.push({name, value})
        optionMatch = experimentOptionsPattern.exec(experimentMatch![1])
      }
        //only use new sim options if there are at least 1
      if(parsedOptions.length>0)
        this.props.parseSimulationOptions(parsedOptions)
    }
  }

  private clearVariableFilter() {
    this.setState({rawVariableFilter: ''})
    this.props.setSimulationVariables([])
    this.props.notifyInfo("Variable filter cleared.")
  }
  private setVariableFilter() {
    if (!R.isEmpty(this.state.rawVariableFilter)) {
      const variables = R.map(s => s.trim(), R.split(/\s+/, this.state.rawVariableFilter))
      this.props.setSimulationVariables(variables)
      this.props.notifyInfo(`variable filter applied. Only include: ${variables.join(',')} in results.`)
    } else {
      console.warn("can't update variable filter if input empty!")
    }
  }

  render() {
    const modelNameChanged = (ev:any) => this.setState({modelName: ev.target.value})
    const variableFilterChanged = (ev:any) => this.setState({rawVariableFilter: ev.target.value})
    return (<>
      <Form validated={this.state.validated} onSubmit={(ev: any) => {ev.preventDefault(); this.simulateClicked(ev) }}>
      <Form.Row>
        <Form.Label column sm={1}>Model</Form.Label>
        <Col sm={10}>
            <Form.Control placeholder="model to simulate" defaultValue={this.openedModelName()} onChange={modelNameChanged} required pattern="[^\s]+"/>
          <Form.Control.Feedback type="invalid">
            Provide a modelname!
          </Form.Control.Feedback>
        </Col>
        <Col sm={1}></Col>
      </Form.Row>
      <Form.Row>
          <Form.Label column sm={1}>Variables</Form.Label>
        <Col sm={10}>
            <Form.Control placeholder="space-separated list of variables to include. Leave empty for all variables." value={this.state.rawVariableFilter} onChange={variableFilterChanged} />
        </Col><Col sm={1} as={ButtonGroup}>
          <Button variant="outline-success" onClick={() => this.setVariableFilter()}><Octicon name='check' /></Button>
          <Button variant="outline-danger" onClick={() => this.clearVariableFilter()}><Octicon name='x' /></Button>
        </Col>
      </Form.Row>
      <SimulationOptions simulateClicked={this.simulateClicked.bind(this)}/>
      </Form>
      </>
    )
  }
}

function mapProps(state: AppState) {
  return { options: state.session!.simulation.options, openFile: state.session!.openedFile, variables: state.session!.simulation.variables }
}

function dispatchToProps(dispatch: (a: Action) => any) {
  return bindActionCreators({ parseSimulationOptions, setSimulationVariables, notifyInfo }, dispatch)
}

const SimulationSetup = connect(mapProps, dispatchToProps)(SimulationSetupCon)
export default SimulationSetup
