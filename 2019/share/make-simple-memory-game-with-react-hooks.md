# How to make a simple memory game with themes in JavaScript using React hooks

原文链接： https://blog.usejournal.com/how-to-make-a-simple-memory-game-with-themes-in-javascript-using-react-hooks-2ecfa9cffe4c

使用Reat hooks开发简单的记忆游戏

## Demo
点击以下链接，查看Demo

https://codesandbox.io/embed/1q92r05x63

## 初始化工程

```
# create-react-app memory-game
```

## 安装依赖

```
# cd memory-game
# npm i --save react@16.7.0-alpha.0 react-dom@16.7.0-alpha.0 styled-components ramda rc-switch
```

## Config

- **levels.json**

```JavaScript

[
    { "cellCount": 3, "memoryCount": 3, "fieldSize": 300, "space": 1 },
    { "cellCount": 4, "memoryCount": 3, "fieldSize": 300, "space": 1 },
    { "cellCount": 4, "memoryCount": 4, "fieldSize": 300, "space": 1 },
    { "cellCount": 5, "memoryCount": 4, "fieldSize": 300, "space": 1 },
    { "cellCount": 5, "memoryCount": 5, "fieldSize": 350, "space": 1 },
    { "cellCount": 5, "memoryCount": 6, "fieldSize": 350, "space": 1 },
    { "cellCount": 5, "memoryCount": 6, "fieldSize": 350, "space": 1 },
    { "cellCount": 5, "memoryCount": 7, "fieldSize": 350, "space": 1 },
    { "cellCount": 6, "memoryCount": 7, "fieldSize": 350, "space": 1 },
    { "cellCount": 6, "memoryCount": 8, "fieldSize": 350, "space": 1 },
    { "cellCount": 6, "memoryCount": 9, "fieldSize": 350, "space": 1 },
    { "cellCount": 6, "memoryCount": 10, "fieldSize": 350, "space": 1 }
]
```

**cellCount** - 表示每行每列的单元格数量，比如`cellCout` = 4，则游戏区域大小为4x4个单元格；

**memoryCount** - 表示用户需要记住的单元格数量

**fieldSize** - 游戏区域大小

**space** - 单元格之间的间隙

- **themes.json**

demo中可以切换深浅主题

```JavaScript
{
    "lightTheme": {
        "header": {
            "height": "50px",
            "background": "#627CA2"
        },
        "body": {
            "bg": "#fff",
            "color": "#000"
        },
        "cell": {
            "bg": "#BDC2E5",
            "activeBg": "#2ecc71",
            "failedBg": "#e74c3c"
        },
        "loader": {
            "bg": "#627CA2"
        }
    },
    "darkTheme": {
        "header": {
            "height": "50px",
            "background": "#627CA2"
        },
        "body": {
            "bg": "#1A1159",
            "color": "#fff"
        },
        "cell": {
            "bg": "#3b2d77",
            "activeBg": "#95C8B9",
            "failedBg": "#DD4A63"
        },
        "loader": {
            "bg": "#627CA2"
        }
    }
}
```

## App files

- utils.js

```
import { path } from 'ramda';

export function getFromTheme (themePath = '') {
    return function getFromThemeProps (props = {}) {
        return path(themePath.split('.'), props.theme);
    }
}
```

`getFromTheme(path)` - 帮助类，获取嵌套结构的props

```JavaScript
// instead of this:

const StyledComponent = styled.div`
  width: ${ ({ theme }) => theme.body.bg }px;
`;

// we will be able to write:

const StyledComponent = styled.div`
  width: ${ getFromTheme('body.bg') }px;
`;
```

- App.jsx

```JavaScript
import React, { useState } from 'react';
import { ThemeProvider, createGlobalStyle } from 'styled-components';
import { getFromTheme } from './utils';
import './index.css';

import Game from './game';
import themes from './config/themes.json';

function App () {
  const [themeName, toggleTheme] = useTheme('darkTheme');

  const GlobalStyle = createGlobalStyle`
    body {
        background: ${getFromTheme('body.bg')};
        color: ${getFromTheme('body.color')};
        transition: background .3s ease;
    }
  `;
  
  return (
    <ThemeProvider theme={themes[themeName]}>
      <React.Fragment>
        <GlobalStyle />
        <Game toggleTheme={toggleTheme} />
      </React.Fragment>
    </ThemeProvider>
  );
}

function useTheme(defaultThemeName) {
  const [themeName, setTheme] = useState(defaultThemeName);

  function switchTheme(name) {
    setTheme(themeName === 'darkTheme' ? 'lightTheme' : 'darkTheme');
  }

  return [themeName, switchTheme];
}


export default App;
```

使用`ThemeProvider`保证每一个component的主题和当前主题一致。

使用React's hook `useState`

- **game/game.reducer.js**

```JavaScript
import { merge } from 'ramda';

import levels from '../config/levels';

export const NEW_LEVEL = 'level/new';
export const HIDDEN_CELL_HIDE = 'hidden/hide';
export const HIDDEN_CELL_SHOW = 'hidden/show';
export const FIELD_HIDE = 'field/hide';
export const FIELD_SHOW = 'field/show';
export const RESET_LEVEL = 'level/reset';

const START_LEVEL = 0;

export const initialState = {
    level: START_LEVEL,
    showHidden: true,
    showField: false,
    levelConfig: levels[START_LEVEL],
}

export function GameReducer(state, action) {
    switch(action.type) {
        case NEW_LEVEL:
            return merge(state, { level: action.level, levelConfig: levels[action.level] });
        case HIDDEN_CELL_SHOW:
            return merge(state, { showHidden: true });
        case HIDDEN_CELL_HIDE:
            return merge(state, { showHidden: false });
        case FIELD_HIDE:
            return merge(state, { showField: false });
        case FIELD_SHOW:
            return merge(state, { showField: true });
        case RESET_LEVEL:
            return merge(initialState, { levelConfig: { ...levels[START_LEVEL] } });
        default:
            return state;
    }
}
```

- **game/index.jsx**

```JavaScript
import React, { memo, useReducer, useMemo, useEffect } from 'react';

import { Field } from './components/GameField';
import { GameFieldView, GameView, SwitchView } from './components/Styled';
import {
    GameReducer, initialState, NEW_LEVEL,
    FIELD_HIDE, FIELD_SHOW, RESET_LEVEL,
} from './game.reducer';
import { generateGameField } from './game.utils';
import Switch from 'rc-switch';

import 'rc-switch/assets/index.css';

function Game ({ toggleTheme }) {
    const [{ level, showHidden, showField, levelConfig }, dispatch] = useReducer(
        GameReducer, initialState
    );

    const { cellCount, memoryCount } = levelConfig;

    const { field, hiddenCells } = useMemo(
        () => generateGameField(cellCount, memoryCount),
        [levelConfig]
    );

    useEffect(
        () => setTimeout(dispatch, 500, { type: FIELD_SHOW }),
        [levelConfig],
    );

    function updateLevel(shouldReset) {
        dispatch({ type: FIELD_HIDE });
        setTimeout(dispatch, 500, { type: shouldReset ? RESET_LEVEL : NEW_LEVEL, level: level + 1 });
    }

    return (
        <GameView>
            <GameFieldView {...levelConfig}>
                <SwitchView>
                    <div>Level: {level}</div>
                    <div>
                        Theme mode: <Switch onClick={toggleTheme} />
                    </div>
                </SwitchView>
                <Field
                    {...levelConfig}
                    levelConfig={levelConfig}
                    visible={showField}
                    key={field}
                    level={level}
                    field={field}
                    hiddenCells={hiddenCells}
                    dispatch={dispatch}
                    showHidden={showHidden}
                    updateLevel={updateLevel}
                />
            </GameFieldView>
        </GameView>
    );
}

export default memo(Game);

```

使用 `useReducer` hook代替Redux

使用 `useMemo` hook记住当前游戏的状态

使用 `useEffect` hook代替`componentDidMount`和`componentDidUpdate`

- **game/game.utils.js**

```JavaScript
export function generateGameField(cellCount, memoryCount) {
    const cellsIndexes = [...Array(cellCount * cellCount)]
        .map((_, i) => i);
    const field = [...cellsIndexes].fill(1);
    const hiddenCells = [];

    for (let i = 0; i < memoryCount; i++) {
        const rNum = Math.floor(Math.random() * cellsIndexes.length);
        const toChange = cellsIndexes.splice(rNum, 1).pop();

        hiddenCells.push(toChange);
        field[toChange] = 2;
    }

    return {
        field, hiddenCells,
    };
}

export const WRONG_GUESSED_CELL = 0;
export const CORRECT_GUESSED_CELL = 3;
export const CELL = 1;
export const HIDDEN_CELL = 2;
```

cellsIndexed - 随机数量的单元格数组

field - 游戏区域

hiddenCells - 隐藏的单元格（需要用户记住的）

- **game/components/GameField.jsx**

```JavaScript

import React, { memo, useState, useEffect } from 'react';
import styled from 'styled-components';


import { HIDDEN_CELL_HIDE, HIDDEN_CELL_SHOW } from '../game.reducer';
import { Cell } from './Cell';
import { WRONG_GUESSED_CELL, CORRECT_GUESSED_CELL } from '../game.utils';

const FieldView = styled.div`
    width: 100%;
    height: 100%;
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    margin: 20px 0;
    opacity: ${({ animationState }) => animationState};
    transform: scale(${({ animationState }) => animationState});
    transition: opacity .2s ease, transform .3s ease;
`;

export const Field = memo(function Field ({
    fieldSize = 0,
    cellCount = 0,
    space = 0,
    field = [],
    hiddenCells = [],
    level = 0,
    showHidden = false,
    dispatch,
    updateLevel,
    visible,
}) {
    const cellSize = fieldSize / cellCount - space;

    const { gameField, onCellClick } = useGameField(field, hiddenCells, updateLevel);
   
    useEffect(
        () => {
            dispatch({ type: HIDDEN_CELL_SHOW })
            setTimeout(() => dispatch({ type: HIDDEN_CELL_HIDE }), 1500);
        },
        [level]
    );

    return (
        <FieldView
            animationState={visible ? 1 : 0}
            onClick={!showHidden ? onCellClick : null}>
            {
                gameField.map((cellValue, i) => (
                    <Cell
                        size={cellSize}
                        space={space}
                        key={i}
                        id={i}
                        value={cellValue}
                        forceShowHidden={showHidden} />
                ))
            }
        </FieldView>
    );
});

function useGameField(field, hiddenCells, updateLevel) {
    const [gameField, setField] = useState(field);
    const [gameHiddenCells, setHidden] = useState(hiddenCells);

    function onCellClick({ target }) {
        const id = Number(target.id);

        if (hiddenCells.includes(id)) {
            const updatedField = gameField.map((e, i) => i === id ? CORRECT_GUESSED_CELL : e);
            const updatedHidden = gameHiddenCells.filter(e => e !== id);

            setField(updatedField);
            setHidden(updatedHidden);

            return !updatedHidden.length && setTimeout(updateLevel, 1000);
        }

        const updatedField = gameField.map((e, i) => i === id ? WRONG_GUESSED_CELL : e);
        setField(updatedField);

        return setTimeout(updateLevel, 1000, true);
    }

    return { gameField, onCellClick };
}


```

使用`useGameField` hook创建两个状态：更新当前游戏各个单元格状态以及跟踪被猜中的单元格

- **game/components/Cell.jsx**

```JavaScript


import React, { memo } from 'react';
import styled from 'styled-components';

import { getFromTheme } from '../../utils';
import { CORRECT_GUESSED_CELL, HIDDEN_CELL } from '../game.utils';

const CellView = styled.div`
    width: ${({ size }) => size}px;
    height: ${({ size }) => size}px;
    background: ${getFromTheme('cell.bg')};
    margin: ${({ space }) => space}px;
    display: flex;
    justify-content: center;
    align-items: center;
`;

const ActiveCellView = styled.div`
    width: ${({ width }) => width}%;
    height: 100%;
    background: ${getFromTheme('cell.activeBg')};
    transition: width .2s ease;
`;

const FailedCellView = styled.div`
    width: ${({ size }) => size}%;
    height: ${({ size }) => size}%;
    background: ${getFromTheme('cell.failedBg')};
    transition: width .2s ease, height .2s ease;
`;

export const Cell = memo(function Cell(props) {
    const { id, value, forceShowHidden } = props;

    const isActive = (forceShowHidden && value === HIDDEN_CELL) || value === CORRECT_GUESSED_CELL;
    const isFailed = !value;
        
    return (
        <CellView {...props}>
            <ActiveCellView id={id} width={isActive ? 100 : 0} />
            <FailedCellView id={id} size={isFailed ? 100 : 0}/>
        </CellView>
    );
});

```

- **game/components/Styled.js**

```JavaScript

import styled from 'styled-components';

export const GameView = styled.div`
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    margin: 80px 0;
`;

export const GameFieldView = styled.div`
    width: ${({ fieldSize, cellCount, space }) => fieldSize + cellCount * space}px;
    height: ${({ fieldSize, cellCount, space }) => fieldSize + cellCount * space}px;
    margin: 20px 0;
`;

export const SwitchView = styled.div`
    display: flex;
    width: 100%;
    justify-content: space-between;
`;

```

查看源码： https://github.com/ivanicharts/memory-game-react
