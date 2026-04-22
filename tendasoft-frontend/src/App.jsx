import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Usuarios from './pages/Usuarios';
import Inventario from './pages/Inventario';
import Caja from './pages/Caja'
import Cierre_caja from './pages/Cierre_caja'
import Facturacion from './pages/Facturacion'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/usuarios" element={<Usuarios />} />
        <Route path="/inventario" element={<Inventario />} />
        <Route path="/caja" element={<Caja />} />
        <Route path="/cierre-caja" element={<Cierre_caja />} />
        <Route path="/facturacion" element={<Facturacion />} />
      </Routes>
    </BrowserRouter>
  );
}