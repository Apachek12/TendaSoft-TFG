import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import {
  Users, Plus, Edit, UserMinus, UserCheck, ArrowLeft, Eye, EyeOff, AlertCircle
} from 'lucide-react';
import { toast, Toaster } from 'react-hot-toast';
import ModalAñadirUsuario from './AñadirUsuario';
import './Avisos.css';

const API = 'http://localhost:8080';

export default function Usuarios() {
  const navigate = useNavigate();

  const [usuarios, setUsuarios]                   = useState([]);
  const [cargando, setCargando]                   = useState(true);
  const [error, setError]                         = useState('');
  const [mostrarArchivados, setMostrarArchivados] = useState(false);
  const [isModalOpen, setIsModalOpen]             = useState(false);
  const [usuarioAEditar, setUsuarioAEditar]       = useState(null);

  useEffect(() => {
    const datosGuardados = localStorage.getItem('usuarioTendaSoft');
    if (!datosGuardados) { navigate('/'); return; }
    const auth = JSON.parse(datosGuardados);
    if (auth.rol !== 'ADMIN') { navigate('/dashboard'); return; }
    cargarUsuarios();
  }, [navigate]);

  const cargarUsuarios = async (mensajeExito = null) => {
    try {
      setCargando(true);
      const { data } = await axios.get(`${API}/api/usuarios`);
      setUsuarios(data);
      if (mensajeExito) toast.success(mensajeExito);
    } catch {
      setError('Error al cargar la lista de usuarios.');
      toast.error('Error al sincronizar usuarios');
    } finally {
      setCargando(false);
    }
  };

  const handleEdit = (e, user) => {
    e.stopPropagation();
    setUsuarioAEditar(user);
    setIsModalOpen(true);
  };

  // Toast de confirmación antes de cambiar el estado del usuario
  const handleToggleStatus = (e, user) => {
    e.stopPropagation();
    const id      = user.idUsuario || user.id;
    const esActivo = user.activo !== false;
    const nombre  = user.nombreReal || user.nombreUsuario;

    toast((t) => (
      <div className="p-6 flex flex-col items-center text-center gap-4">
        <div className="w-12 h-12 bg-teal-50 rounded-full flex items-center justify-center text-teal-600">
          <AlertCircle size={28} />
        </div>
        <div>
          <h3 className="text-lg font-black text-slate-800 tracking-tight">Cambiar estado</h3>
          <p className="text-sm font-medium text-slate-500 mt-1">
            {esActivo
              ? `¿Deshabilitar el acceso a ${nombre}?`
              : `¿Habilitar el acceso para ${nombre}?`}
          </p>
        </div>
        <div className="flex w-full gap-3 mt-2">
          <button
            onClick={() => toast.dismiss(t.id)}
            className="flex-1 px-4 py-3 bg-slate-100 text-slate-500 text-xs font-black rounded-xl hover:bg-slate-200 uppercase tracking-widest transition-colors"
          >
            Cancelar
          </button>
          <button
            onClick={async () => {
              toast.dismiss(t.id);
              try {
                await axios.delete(`${API}/api/usuarios/${id}`);
                cargarUsuarios(esActivo ? 'Acceso deshabilitado' : 'Acceso habilitado');
              } catch {
                toast.error('No se pudo realizar la operación');
              }
            }}
            className="flex-1 px-4 py-3 bg-teal-700 text-white text-xs font-black rounded-xl hover:bg-teal-800 uppercase tracking-widest transition-all shadow-lg shadow-teal-700/20"
          >
            Confirmar
          </button>
        </div>
      </div>
    ), { duration: Infinity, position: 'top-center', className: 'toast-confirmacion-centro' });
  };

  const usuariosFiltrados = usuarios.filter(u => mostrarArchivados || u.activo !== false);

  return (
    <div className="min-h-screen bg-slate-50 p-10 font-sans antialiased">
      <Toaster position="top-right" reverseOrder={false} />

      <div className="max-w-5xl mx-auto">

        <div className="flex items-center justify-between mb-10 flex-shrink-0">
          <div className="flex items-center">
            <button
              onClick={() => navigate('/dashboard')}
              className="mr-4 p-2 text-slate-500 hover:bg-slate-200 hover:text-slate-800 rounded-full transition-all"
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
              <Plus className="w-5 h-5 mr-2" /> Añadir Usuario
            </button>
          </div>
        </div>

        {cargando && <p className="text-center text-gray-500 animate-pulse py-10">Cargando usuarios...</p>}
        {error    && <div className="bg-red-50 text-red-600 p-4 rounded-xl text-center border border-red-100 mb-6">{error}</div>}

        <div className="space-y-4">
          {!cargando && !error && usuariosFiltrados.map(user => (
            <div
              key={user.idUsuario}
              className={`p-5 rounded-2xl flex items-center transition-all group ${
                user.activo === false
                  ? 'bg-slate-50/50 opacity-60 grayscale border border-dashed border-slate-300'
                  : 'bg-white border border-slate-100 shadow-sm hover:shadow-md'
              }`}
            >
              <div className={`w-12 h-12 rounded-xl flex items-center justify-center font-black text-xl mr-5 transition-transform ${
                user.activo === false
                  ? 'bg-slate-200 text-slate-500'
                  : 'bg-teal-50 text-teal-700 group-hover:scale-105'
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
                <p className="text-xs font-bold text-teal-600 tracking-wider">@{user.nombreUsuario}</p>
              </div>

              <div className="mr-8">
                <span className={`px-4 py-1.5 rounded-lg text-[10px] font-black tracking-widest uppercase shadow-sm ${
                  user.rol === 'ADMIN'
                    ? 'bg-[#00796B] text-white'
                    : 'bg-white text-[#00796B] border border-[#00796B]'
                }`}>
                  {user.rol || 'VENDEDOR'}
                </span>
              </div>

              <div className="flex items-center space-x-1 border-l pl-6 border-slate-100">
                <button onClick={e => handleEdit(e, user)} className="p-2 text-slate-300 hover:text-teal-600 hover:bg-teal-50 rounded-lg transition-all">
                  <Edit className="w-5 h-5" />
                </button>
                <button onClick={e => handleToggleStatus(e, user)} className={`p-2 rounded-lg transition-all ${
                  user.activo === false
                    ? 'text-teal-400 hover:text-teal-600 hover:bg-teal-50'
                    : 'text-red-200 hover:text-red-500 hover:bg-red-50'
                }`}>
                  {user.activo === false ? <UserCheck className="w-5 h-5" /> : <UserMinus className="w-5 h-5" />}
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>

      <ModalAñadirUsuario
        isOpen={isModalOpen}
        onClose={() => { setIsModalOpen(false); setUsuarioAEditar(null); }}
        onSuccess={() => cargarUsuarios(usuarioAEditar ? 'Usuario actualizado' : 'Usuario añadido')}
        usuarioEdit={usuarioAEditar}
      />
    </div>
  );
}