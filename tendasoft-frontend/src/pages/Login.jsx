import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const API = 'http://localhost:8080';

export default function Login() {
  const navigate = useNavigate();

  const [usuario, setUsuario]     = useState('');
  const [contrasena, setContrasena] = useState('');
  const [error, setError]         = useState('');
  const [cargando, setCargando]   = useState(false);

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');

    if (!usuario || !contrasena) {
      setError('Por favor, rellena todos los campos.');
      return;
    }

    setCargando(true);
    try {
      const { data } = await axios.post(`${API}/api/usuarios/login`, {
        nombreUsuario: usuario,
        contrasena
      });
      localStorage.setItem('usuarioTendaSoft', JSON.stringify(data));
      navigate('/dashboard');
    } catch (err) {
      if (err.response?.status === 401) {
        setError('Usuario o contraseña incorrectos.');
      } else if (err.response?.status === 404) {
        setError('El usuario no existe en el sistema.');
      } else if (err.response) {
        setError('Error interno del servidor.');
      } else {
        setError('No se pudo conectar con el servidor.');
      }
    } finally {
      setCargando(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-center items-center">
      <div className="mb-8">
        <img src="/src/logo-tendasoft.png" alt="TendaSoft" className="w-24 h-24" />
      </div>

      <div className="bg-white p-10 rounded-2xl shadow-sm w-full max-w-md border border-gray-100">
        <form onSubmit={handleLogin} className="flex flex-col space-y-6">
          <input
            type="text"
            placeholder="Usuario"
            value={usuario}
            onChange={e => setUsuario(e.target.value)}
            disabled={cargando}
            className="w-full bg-gray-200 text-gray-700 rounded-lg px-4 py-3 focus:outline-none focus:ring-2 focus:ring-orange-400 disabled:opacity-50"
          />
          <input
            type="password"
            placeholder="Contraseña"
            value={contrasena}
            onChange={e => setContrasena(e.target.value)}
            disabled={cargando}
            className="w-full bg-gray-200 text-gray-700 rounded-lg px-4 py-3 focus:outline-none focus:ring-2 focus:ring-orange-400 disabled:opacity-50"
          />

          {error && (
            <div className="bg-red-50 text-red-500 text-sm text-center p-3 rounded-lg border border-red-100">
              {error}
            </div>
          )}

          <div className="flex justify-center pt-4">
            <button
              type="submit"
              disabled={cargando}
              className="bg-orange-500 hover:bg-orange-600 text-white font-medium px-8 py-2 rounded-lg transition-colors disabled:bg-orange-300"
            >
              {cargando ? 'Comprobando...' : 'Iniciar sesión'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}