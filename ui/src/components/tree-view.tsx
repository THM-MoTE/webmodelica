import React from 'react'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
//@ts-ignore
import Octicon from 'react-octicon'
//@ts-ignore
import { Treebeard, decorators } from 'react-treebeard';
import { SplitButton, Dropdown, Button } from 'react-bootstrap'
import { AppState, FileNode, File, setId, CompilerError } from '../models/index'

interface Props {
  tree: FileNode
  compilerErrors: CompilerError[]
  deleteFile(f:File): void
  renameFile(f:File): void
  onFileClicked(f: File): void
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

  constructor(p:Props) {
    super(p)
    this.state = {}
    this.onToggle = this.onToggle.bind(this);
  }

  private errorsInFile = (path: string) => this.props.compilerErrors.filter(e => e.file == path)

  Header = (obj: any) => {
    const {style, node} = obj
    const iconName = node.children ? 'file-directory' : 'file-text';

    if(node.children) {
      //if directory doesn't contain children; set text color=grey
      const btnClass = (node.children.length<=0) ? 'text-secondary' : undefined
      return (
        <Button key={node.path} variant='link' className={btnClass}>
          <Octicon name={iconName} /> {node.path}
        </Button>
      )
    } else {
      const errorCount = this.errorsInFile(node.path).length
      const btnTitle = (<span>
        { (errorCount>0) && (<span className="badge badge-danger treeViewError">{errorCount}</span>) }
        { node.path }
        </span>
      )
      return (
        <SplitButton
          title={btnTitle}
          onClick={() => this.props.onFileClicked(node.file)}
          key={node.path+"/"+node.path}
          size="sm"
          id={`file-view-dropdown-${node.name}`}
          variant="link">
          <Dropdown.Item className="text-warning" onClick={() => this.props.renameFile(node.file)}><Octicon name="pencil" /> Rename</Dropdown.Item>
          <Dropdown.Item className="text-danger" onClick={() => this.props.deleteFile(node.file)}><Octicon name="x" /> Delete</Dropdown.Item>
        </SplitButton>
      );
    }
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
    tree: state.session!.files,
    compilerErrors: state.session!.compilerErrors
  }
}

export const TreeView = connect(mapProps, null)(TreeViewCon)
