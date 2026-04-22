import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Users, Plus, Edit, UserMinus, UserCheck, ArrowLeft, Eye, EyeOff } from 'lucide-react';
import ModalAñadirUsuario from './AñadirUsuario';

export default function Usuarios() {
  const [usuarios, setUsuarios] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [mostrarArchivados, setMostrarArchivados] = useState(false);
  const navigate = useNavigate();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [usuarioAEditar, setUsuarioAEditar] = useState(null);

  const cargarUsuarios = async () => {
    try {
      setCargando(true);
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

  const handleEdit = (e, user) => {
    e.stopPropagation();
    setUsuarioAEditar(user);
    setIsModalOpen(true);
  };

  const handleToggleStatus = async (e, user) => {
    e.stopPropagation();
    const id = user.idUsuario || user.id;
    const esActivo = user.activo !== false;

    const mensaje = esActivo
      ? `¿Seguro que deseas deshabilitar a "${user.nombreReal || user.nombreUsuario}"?`
      : `¿Deseas restaurar el acceso a "${user.nombreReal || user.nombreUsuario}"?`;

    if (window.confirm(mensaje)) {
      try {
        await axios.delete(`http://localhost:8080/api/usuarios/${id}`);
        cargarUsuarios();
      } catch (err) {
        alert("Error al cambiar el estado del usuario.");
      }
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 p-10 font-sans">
      <div className="max-w-5xl mx-auto">

        {/* === CABECERA === */}
        <div className="flex items-center justify-between mb-10 flex-shrink-0">
          <div className="flex items-center">
            <button
              onClick={() => navigate('/dashboard')}
              className="mr-4 p-2 text-slate-500 hover:bg-slate-200 hover:text-slate-800 rounded-full transition-all"
              title="Volver al inicio"
            >
              <ArrowLeft size={24} />
            </button>

            <div className="flex items-center text-slate-800 border-l pl-4 border-slate-200">
              <Users className="w-8 h-8 text-slate-700 mr-3" />
              <h1 className="text-2xl font-bold tracking-tight">Gestión de Usuarios</h1>
            </div>
          </div>

          <div className="flex items-center space-x-3">
            <button
              onClick={() => setMostrarArchivados(!mostrarArchivados)}
              className="bg-white border border-slate-200 text-teal-700 px-4 py-2.5 rounded-lg font-bold hover:bg-slate-100 transition-all flex items-center text-xs tracking-widest uppercase shadow-sm"
            >
              {mostrarArchivados ? <Eye size={18} className="mr-2" /> : <EyeOff size={18} className="mr-2" />}
              {mostrarArchivados ? 'Ocultar Inactivos' : 'Ver Inactivos'}
            </button>

            <button
              onClick={() => { setUsuarioAEditar(null); setIsModalOpen(true); }}
              className="bg-teal-700 hover:bg-teal-800 text-white px-5 py-2.5 rounded-lg flex items-center font-bold transition-all shadow-md text-xs tracking-widest uppercase"
            >
              <Plus className="w-5 h-5 mr-2" />
              Añadir Usuario
            </button>
          </div>
        </div>

        {cargando && <p className="text-center text-gray-500 animate-pulse py-10">Cargando usuarios...</p>}
        {error && (
          <div className="bg-red-50 text-red-600 p-4 rounded-xl text-center border border-red-100 mb-6">
            {error}
          </div>
        )}

        {/* === LISTADO === */}
        <div className="space-y-4">
          {!cargando && !error && usuarios
            .filter(u => mostrarArchivados ? true : u.activo !== false)
            .map((user) => (
            <div
              key={user.idUsuario}
              className={`p-5 rounded-2xl flex items-center transition-all group ${
                user.activo === false
                ? 'bg-slate-50/50 opacity-60 grayscale border border-dashed border-slate-300'
                : 'bg-white border border-slate-100 shadow-sm hover:shadow-md'
              }`}
            >

              <div className={`w-12 h-12 rounded-xl flex items-center justify-center font-black text-xl mr-5 transition-transform ${
                user.activo === false ? 'bg-slate-200 text-slate-500' : 'bg-teal-50 text-teal-700 group-hover:scale-105'
              }`}>
                {(user.nombreReal || user.nombreUsuario).charAt(0).toUpperCase()}
              </div>

              <div className="flex-1">
                <div className="flex items-center">
                  <p className="font-black text-slate-800 text-lg leading-tight uppercase">
                    {user.nombreReal || 'Sin Nombre Asignado'}
                  </p>
                  {user.activo === false && (
                    <span className="ml-3 text-[9px] font-black bg-slate-200 text-slate-500 px-2 py-0.5 rounded uppercase tracking-widest">
                      Inactivo
                    </span>
                  )}
                </div>
                <p className="text-xs font-bold text-teal-600 tracking-wider">
                  @{user.nombreUsuario}
                </p>
              </div>

              {/* === COLORES DE ROL ACTUALIZADOS === */}
              <div className="mr-8">
                <span className={`px-4 py-1.5 rounded-lg text-[10px] font-black tracking-widest uppercase shadow-sm ${
                  user.rol === 'ADMIN'
                    ? 'bg-[#00796B] text-white border border-[#00796B]' // Fondo Verde, Letras Blancas
                    : 'bg-white text-[#00796B] border border-[#00796B]' // Fondo Blanco, Letras Verdes
                }`}>
                  {user.rol || 'VENDEDOR'}
                </span>
              </div>

              <div className="flex items-center space-x-1 border-l pl-6 border-slate-100">
                <button
                  onClick={(e) => handleEdit(e, user)}
                  className="p-2 text-slate-300 hover:text-teal-600 hover:bg-teal-50 rounded-lg transition-all"
                  title="Editar"
                >
                  <Edit className="w-5 h-5" />
                </button>
                <button
                  onClick={(e) => handleToggleStatus(e, user)}
                  className={`p-2 rounded-lg transition-all ${
                    user.activo === false
                    ? 'text-teal-400 hover:text-teal-600 hover:bg-teal-50'
                    : 'text-red-200 hover:text-red-500 hover:bg-red-50'
                  }`}
                  title={user.activo === false ? "Restaurar" : "Deshabilitar"}
                >
                  {user.activo === false ? <UserCheck className="w-5 h-5" /> : <UserMinus className="w-5 h-5" />}
                </button>
              </div>

            </div>
          ))}

          {!cargando && !error && usuarios.filter(u => mostrarArchivados ? true : u.activo !== false).length === 0 && (
            <div className="text-center py-20 bg-white rounded-3xl border-2 border-dashed border-slate-100">
                <Users className="w-12 h-12 text-slate-200 mx-auto mb-4" />
                <p className="text-slate-400 font-medium">No hay usuarios para mostrar.</p>
            </div>
          )}
        </div>
      </div>

      <ModalAñadirUsuario
        isOpen={isModalOpen}
        onClose={() => { setIsModalOpen(false); setUsuarioAEditar(null); }}
        onSuccess={cargarUsuarios}
        usuarioEdit={usuarioAEditar}
      />
    </div>
  );
}