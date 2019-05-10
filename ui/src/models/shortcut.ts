import * as monaco from 'monaco-editor';
import * as R from 'ramda';

export interface Shortcut {
  modifiers: number[]
  key: number
  callback():void
}

export function cmdAnd(key:number, fn:() => void):Shortcut {
  return {
    modifiers: [monaco.KeyMod.CtrlCmd],
    key: key,
    callback: fn
  }
}

export function cmdShiftAnd(key: number, fn: () => void):Shortcut {
  return {
    modifiers: [monaco.KeyMod.CtrlCmd, monaco.KeyMod.Shift],
    key: key,
    callback: fn
  }
}

export function asKeyBinding(s:Shortcut):number {
  return R.reduce<number,number>(R.add, s.key, s.modifiers)
}
