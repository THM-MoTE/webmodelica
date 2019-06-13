import React from 'react'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
//@ts-ignore
import Octicon from 'react-octicon'
//@ts-ignore
import { Treebeard, decorators } from 'react-treebeard';
import { SplitButton, Dropdown, Button, ButtonGroup } from 'react-bootstrap'
import { AppState, FileNode, File, FilePath, setId, CompilerError } from '../models/index'
import * as R from 'ramda';

interface Props {
  tree: FileNode
  compilerErrors: CompilerError[]
  onFileClicked: (f: File) => void
  deleteFile?: (f:File) => void
  renameFile?: (f: File) => void
}

interface State {
  cursor?: any
  active?: boolean
}

// this is a modified version of Treebeard's default style.
//source: https://github.com/storybooks/react-treebeard/blob/master/src/themes/default.js
const treeStyle = {
  tree: {
    base: {
      listStyle: 'none',
      backgroundColor: 'white',
      margin: 0,
      padding: 0,
      color: 'black',
      fontFamily: 'lucida grande ,tahoma,verdana,arial,sans-serif',
      fontSize: '14px'
    },
    node: {
      base: {
        position: 'relative'
      },
      link: {
        cursor: 'pointer',
        position: 'relative',
        padding: '0px 5px',
        display: 'block'
      },
      activeLink: {
       background: 'white'
      },
      toggle: {
        base: {
          position: 'relative',
          display: 'inline-block',
          verticalAlign: 'top',
          marginLeft: '-5px',
          height: '24px',
          width: '24px'
        },
        wrapper: {
          position: 'absolute',
          top: '50%',
          left: '50%',
          margin: '-7px 0 0 -7px',
          height: '14px'
        },
        height: 12,
        width: 12,
        arrow: {
          fill: '#9DA5AB',
          strokeWidth: 0
        }
      },
      header: {
        base: {
          display: 'inline-block',
          verticalAlign: 'top',
          color: '#9DA5AB'
        },
        connector: {
          width: '2px',
          height: '12px',
          borderLeft: 'solid 2px black',
          borderBottom: 'solid 2px black',
          position: 'absolute',
          top: '0px',
          left: '-21px'
        },
        title: {
          lineHeight: '24px',
          verticalAlign: 'middle'
        }
      },
      subtree: {
        listStyle: 'none',
        paddingLeft: '19px'
      },
      loading: {
        color: '#E2C089'
      }
    }
  }
}


/** the TreeView left of editor below the Action buttons. */
export class TreeViewCon extends React.Component<Props,State> {

  /* indicates if at least one of the props functions is defined */
  private readonly oneSplitButtonFunctionDefined :boolean

  constructor(p:Props) {
    super(p)
    this.state = {}
    this.onToggle = this.onToggle.bind(this);
    this.oneSplitButtonFunctionDefined = R.any((fn) => fn !== undefined, [this.props.renameFile, this.props.deleteFile])
  }

  private errorsInFile = (path: string) => this.props.compilerErrors.filter(e => e.file == path)

  Header = (obj: any) => {
    const {style, node} = obj
    const iconName = node.children ? 'file-directory' : 'file-text';
    const errorCount = this.errorsInFile(node.path).length
    const btnTitle = (<span>
      { (errorCount>0) && (<span className="badge badge-danger treeViewError">{errorCount}</span>) }
      <Octicon name={iconName} /> { node.path }
      </span>
    )

    //if the node is a directory, there is no 'file' property, however we need it to rename or delete the directory
    node.file = (node.file) ? node.file : {relativePath: node.path, content: ""}

    const splitButton = () => (
      <SplitButton
        title={btnTitle}
        onClick={() => this.props.onFileClicked(node.file) }
        key={node.path+"/"+node.path}
        size="sm"
        id={`file-view-dropdown-${node.name}`}
        variant="link"
        >
        {this.props.renameFile && <Dropdown.Item className="text-warning" onClick={() => this.props.renameFile!(node.file)}><Octicon name="pencil" /> Rename</Dropdown.Item>}
        {this.props.deleteFile && <Dropdown.Item className="text-danger" onClick={() => this.props.deleteFile!(node.file)}><Octicon name="x" /> Delete</Dropdown.Item>}
      </SplitButton>
    )
    const button = () => (
      <Button
        onClick={() => this.props.onFileClicked(node.file)}
        key={node.path + "/" + node.path}
        size="sm"
        id={`file-view-dropdown-${node.name}`}
        variant="link">
          {btnTitle}
      </Button>
    )
    //create a SplitButton on demand if at least one of the functions is defined; create a Button otherwise
    //(we don't need a SplitButton if the functions aren't defined)
    return (this.oneSplitButtonFunctionDefined) ? splitButton() : button()
  };

  onToggle(node: any, toggled: boolean) {
    const { cursor } = this.state;

    if (cursor) {
      this.setState({ cursor, active: false });
    }

    node.active = true;
    if (node.children) {
      node.toggled = toggled;
    }

    this.setState({ cursor: node });
  }

  render() {
    const Header = this.Header
    return (
    <Treebeard
      data={this.props.tree}
      style={treeStyle}
      decorators={{...decorators, Header}}
      onToggle={this.onToggle}
    />
    )
  }
}

function mapProps(state: AppState) {
  return {
  }
}

export const TreeView = connect(null, null)(TreeViewCon)
