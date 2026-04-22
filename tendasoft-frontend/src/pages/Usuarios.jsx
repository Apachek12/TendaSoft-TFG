import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Users, Plus, Edit, Trash2, ArrowLeft } from 'lucide-react';
import ModalAñadirUsuario from './AñadirUsuario';

export default function Usuarios() {
  const [usuarios, setUsuarios] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const [isModalOpen, setIsModalOpen] = useState(false);

  const cargarUsuarios = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/usuarios');
      setUsuarios(response.data);
    } catch (err) {
      setError('Error al cargar la lista de usuarios. Comprueba que el backend esté encendido.');
    } finally {
      setCargando(false);
    }
  };

  useEffect(() => {
    const usuarioGuardado = localStorage.getItem('usuarioTendaSoft');
    if (!usuarioGuardado) {
      navigate('/');
      return;
    }

    const usuario = JSON.parse(usuarioGuardado);
    if (usuario.rol !== 'ADMIN') {
      navigate('/dashboard');
      return;
    }

    cargarUsuarios();
  }, [navigate]);

  return (
    <div className="min-h-screen bg-slate-50 p-10 font-sans">
      <div className="max-w-5xl mx-auto">

        {/* === CABECERA UNIFICADA (Estilo Inventario/Caja) === */}
        <div className="flex items-center justify-between mb-10 flex-shrink-0">
          <div className="flex items-center">
            {/* Botón Volver circular */}
            <button
              onClick={() => navigate('/dashboard')}
              className="mr-4 p-2 text-slate-500 hover:bg-slate-200 hover:text-slate-800 rounded-full transition-all"
              title="Volver al inicio"
            >
              <ArrowLeft size={24} />
            </button>

            {/* Separador y Título */}
            <div className="flex items-center text-slate-800 border-l pl-4 border-slate-200">
              <Users className="w-8 h-8 text-slate-700 mr-3" />
              <h1 className="text-2xl font-bold tracking-tight">Gestión de Usuarios</h1>
            </div>
          </div>

          {/* Botón Añadir (Mantenido a la derecha para no saturar la izquierda) */}
          <button
            onClick={() => setIsModalOpen(true)}
            className="bg-teal-700 hover:bg-teal-800 text-white px-5 py-2.5 rounded-lg flex items-center font-bold transition-all shadow-sm text-xs tracking-widest uppercase"
          >
            <Plus className="w-5 h-5 mr-2" />
            Añadir Usuario
          </button>
        </div>

        {/* Mensajes de carga o error */}
        {cargando && <p className="text-center text-gray-500 animate-pulse py-10">Cargando usuarios...</p>}
        {error && (
          <div className="bg-red-50 text-red-600 p-4 rounded-xl text-center border border-red-100 mb-6">
            {error}
          </div>
        )}

        {/* Lista de Usuarios */}
        <div className="space-y-4">
          {!cargando && !error && usuarios.map((user) => (
            <div key={user.idUsuario} className="bg-white p-5 rounded-2xl shadow-sm border border-slate-100 flex items-center hover:shadow-md transition-all group">

              {/* Avatar con la inicial del Nombre Real */}
              <div className="w-12 h-12 rounded-xl bg-teal-50 text-teal-700 flex items-center justify-center font-black text-xl mr-5 group-hover:scale-105 transition-transform">
                {(user.nombreReal || user.nombreUsuario).charAt(0).toUpperCase()}
              </div>

              {/* Info del usuario corregida con nombreReal */}
              <div className="flex-1">
                <p className="font-black text-slate-800 text-lg leading-tight uppercase">
                  {user.nombreReal || 'Sin Nombre Asignado'}
                </p>
                <p className="text-xs font-bold text-teal-600 tracking-wider">
                  @{user.nombreUsuario}
                </p>
              </div>

              {/* Etiqueta de Rol */}
              <div className="mr-8">
                <span className={`px-4 py-1.5 rounded-lg text-[10px] font-black tracking-widest uppercase ${
                  user.rol === 'ADMIN'
                    ? 'bg-amber-50 text-amber-600 border border-amber-100'
                    : 'bg-slate-100 text-slate-500'
                }`}>
                  {user.rol || 'VENDEDOR'}
                </span>
              </div>

              {/* Botones de acción */}
              <div className="flex items-center space-x-2 border-l pl-6 border-slate-50">
                <button className="p-2 text-slate-300 hover:text-slate-600 hover:bg-slate-50 rounded-lg transition-all">
                  <Edit className="w-5 h-5" />
                </button>
                <button className="p-2 text-red-200 hover:text-red-500 hover:bg-red-50 rounded-lg transition-all">
                  <Trash2 className="w-5 h-5" />
                </button>
              </div>

            </div>
          ))}

          {/* Mensaje por si la lista está vacía */}
          {!cargando && usuarios.length === 0 && (
            <div className="text-center py-20 bg-white rounded-3xl border-2 border-dashed border-slate-100">
                <Users className="w-12 h-12 text-slate-200 mx-auto mb-4" />
                <p className="text-slate-400 font-medium">No hay usuarios registrados.</p>
            </div>
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