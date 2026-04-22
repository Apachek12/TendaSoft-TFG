import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Usuarios from './pages/Usuarios';
import Inventario from './pages/Inventario';
import Caja from './pages/Caja'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Cuando entremos a la raíz (/), cargamos el Login */}
        <Route path="/" element={<Login />} />

        {/* Dejamos preparado el Dashboard para cuando lo conectemos */}
        <Route path="/dashboard" element={<Dashboard />} />

        <Route path="/usuarios" element={<Usuarios />} />

        <Route path="/inventario" element={<Inventario />} />

        <Route path="/caja" element={<Caja />} />
      </Routes>
    </BrowserRouter>
  );
}