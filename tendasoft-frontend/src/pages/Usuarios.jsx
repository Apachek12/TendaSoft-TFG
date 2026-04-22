import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Users, Plus, Edit, Trash2 } from 'lucide-react';
import ModalAñadirUsuario from './AñadirUsuario';

export default function Usuarios() {
  const [usuarios, setUsuarios] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const [isModalOpen, setIsModalOpen] = useState(false);

const cargarUsuarios = async () => {
      try {
        // Asegúrate de que esta URL coincida con tu backend
        const response = await axios.get('http://localhost:8080/api/usuarios');
        setUsuarios(response.data);
      } catch (err) {
        setError('Error al cargar la lista de usuarios. Comprueba que el backend esté encendido.');
      } finally {
        setCargando(false);
      }
    };
  useEffect(() => {
    // 1. PROTECCIÓN DE RUTA: Comprobamos si es ADMIN
    const usuarioGuardado = localStorage.getItem('usuarioTendaSoft');
    if (!usuarioGuardado) {
      navigate('/'); // Si no está logueado, al login
      return;
    }

    const usuario = JSON.parse(usuarioGuardado);
    if (usuario.rol !== 'ADMIN') {
      navigate('/dashboard'); // Si no es admin, al dashboard
      return;
    }

    // 2. CARGAR USUARIOS DESDE SPRING BOOT


    cargarUsuarios();
  }, [navigate]);

  return (
    <div className="min-h-screen bg-slate-50 p-10 font-sans">
      <div className="max-w-5xl mx-auto">

        {/* Cabecera */}
        <div className="flex justify-between items-center mb-8">
          <div className="flex items-center text-slate-800">
            <Users className="w-8 h-8 mr-3" />
            <h1 className="text-2xl font-bold">Gestión de Usuarios</h1>
          </div>

          <button
            onClick={() => setIsModalOpen(true)}
            className="bg-teal-700 hover:bg-teal-800 text-white px-5 py-2.5 rounded-lg flex items-center font-medium transition-colors shadow-sm"
          >
            <Plus className="w-5 h-5 mr-2" />
            AÑADIR USUARIO
          </button>
        </div>

        {/* Mensajes de carga o error */}
        {cargando && <p className="text-center text-gray-500">Cargando usuarios...</p>}
        {error && <p className="text-center text-red-500 bg-red-50 p-4 rounded-lg border border-red-100">{error}</p>}

        {/* Lista de Usuarios */}
        <div className="space-y-4">
          {!cargando && !error && usuarios.map((user) => (
            <div key={user.idUsuario} className="bg-white p-5 rounded-xl shadow-sm border border-gray-100 flex items-center hover:shadow-md transition-shadow">

              {/* Avatar (Muestra la primera letra del nombre) */}
              <div className="w-12 h-12 rounded-full bg-teal-50 text-teal-800 flex items-center justify-center font-bold text-xl mr-5">
                {user.nombreUsuario ? user.nombreUsuario.charAt(0).toUpperCase() : 'U'}
              </div>

              {/* Info del usuario */}
              <div className="flex-1">
                {/* Si no tienes un campo "nombreReal", mostramos el nombre de usuario de nuevo o un placeholder */}
                <p className="font-bold text-slate-800 text-lg">
                  {user.nombreUsuario || 'Usuario Desconocido'}
                </p>
                <p className="text-sm text-gray-400">
                  {user.nombreUsuario || 'sin_usuario'}
                </p>
              </div>

              {/* Etiqueta de Rol */}
              <div className="mr-8">
                <span className={`px-4 py-1.5 rounded-full text-xs font-bold tracking-wide ${
                  user.rol === 'ADMIN'
                    ? 'bg-teal-50 text-teal-700'
                    : 'bg-gray-100 text-gray-500'
                }`}>
                  {user.rol || 'VENDEDOR'}
                </span>
              </div>

              {/* Botones de acción */}
              <div className="flex space-x-4">
                <button className="text-slate-400 hover:text-slate-600 transition-colors">
                  <Edit className="w-5 h-5" />
                </button>
                <button className="text-red-400 hover:text-red-600 transition-colors">
                  <Trash2 className="w-5 h-5" />
                </button>
              </div>

            </div>
          ))}

          {/* Mensaje por si la lista está vacía */}
          {!cargando && usuarios.length === 0 && (
            <p className="text-center text-gray-400 py-10">No hay usuarios registrados en el sistema.</p>
          )}
        </div>

      </div>


    <ModalAñadirUsuario
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSuccess={cargarUsuarios}
    />
    </div>
  );
}