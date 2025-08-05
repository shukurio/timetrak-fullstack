import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'

function App() {
    const [count, setCount] = useState(0)

    return (
        <>
            <div className="flex justify-center items-center gap-4 mb-8">
                <a href="https://vite.dev" target="_blank">
                    <img src={viteLogo} className="logo" alt="Vite logo" />
                </a>
                <a href="https://react.dev" target="_blank">
                    <img src={reactLogo} className="logo react" alt="React logo" />
                </a>
            </div>
            <h1 className="text-4xl font-bold text-center mb-8">Vite + React</h1>
            <div className="card">
                <button
                    className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded transition-colors"
                    onClick={() => setCount((count) => count + 1)}
                >
                    count is {count}
                </button>
                <p className="mt-4">
                    Edit <code>src/App.jsx</code> and save to test HMR
                </p>
            </div>
            <p className="read-the-docs">
                Click on the Vite and React logos to learn more
            </p>
            <div className="bg-gradient-to-r from-purple-400 via-pink-500 to-red-500 text-white p-4 rounded-lg shadow-lg">
                <h2 className="text-xl font-bold">Tailwind is working! 🎉</h2>
            </div>
        </>
    )
}

export default App