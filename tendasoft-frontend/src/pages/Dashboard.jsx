import React, { useState, useEffect, useRef } from 'react'; // 1. Añadimos useRef
import Topbar from './Topbar';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Lock, MonitorSmartphone, Package, Users, LogOut, AlertTriangle } from 'lucide-react';
import { toast, Toaster } from 'react-hot-toast';
import './Avisos.css';

export default function Dashboard() {
  const navigate = useNavigate();
  const [usuario, setUsuario] = useState(null);

  // BANDERA DE CONTROL: Evita que el toast salga dos veces en el montaje
  const toastLanzado = useRef(false);

  useEffect(() => {
    const datosGuardados = localStorage.getItem('usuarioTendaSoft');
    if (datosGuardados) {
      setUsuario(JSON.parse(datosGuardados));

      // Solo ejecutamos la verificación si no se ha lanzado ya el toast en este montaje
      if (!toastLanzado.current) {
        verificarStock();
        toastLanzado.current = true; // Bloqueamos futuros lanzamientos
      }
    } else {
      navigate('/');
    }
  }, [navigate]);

  const verificarStock = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/productos');
      const criticos = response.data.filter(p => p.unidades < 50 && p.activo !== false);

      if (criticos.length > 0) {
        const listaNombres = criticos.map(p => p.nombre).join(', ');

        toast.error(`Stock bajo: ${listaNombres}`, {
          duration: 10000,
          icon: <AlertTriangle className="text-red-500" size={20} />,
          style: {
            borderRadius: '12px',
            fontSize: '13px',
            fontWeight: 'bold',
            maxWidth: '450px',
            border: '1px solid #fee2e2'
          }
        });
      }
    } catch (error) {
      console.error("Error al verificar stock", error);
    }
  };

  // --- LÓGICA DE CERRAR SESIÓN ---
  const handleLogout = () => {
    toast((t) => (
      <div className="p-6 flex flex-col items-center text-center gap-4">
        <div className="w-12 h-12 bg-red-50 rounded-full flex items-center justify-center text-red-600">
          <LogOut size={28} />
        </div>
        <div>
          <h3 className="text-lg font-black text-slate-800 tracking-tight uppercase">¿Cerrar Sesión?</h3>
        </div>
        <div className="flex w-full gap-3 mt-2">
          <button onClick={() => toast.dismiss(t.id)} className="flex-1 px-4 py-3 bg-slate-100 text-slate-500 text-xs font-black rounded-xl hover:bg-slate-200 uppercase tracking-widest transition-colors">
            Cancelar
          </button>
          <button
            onClick={() => {
              toast.dismiss(t.id);
              localStorage.removeItem('usuarioTendaSoft');
              navigate('/');
            }}
            className="flex-1 px-4 py-3 bg-red-600 text-white text-xs font-black rounded-xl hover:bg-red-700 uppercase tracking-widest shadow-lg shadow-red-200 transition-all"
          >
            Confirmar
          </button>
        </div>
      </div>
    ), {
      duration: Infinity,
      position: 'top-center',
      className: 'toast-confirmacion-centro'
    });
  };

  if (!usuario) return null;

  const MenuCard = ({ icon, title, onClick }) => (
    <button
      onClick={onClick}
      className="bg-white flex flex-col items-center justify-center p-12 rounded-3xl shadow-sm border border-gray-100 hover:shadow-md hover:border-blue-100 transition-all aspect-[4/3] group"
    >
      <div className="mb-6 p-4 rounded-full bg-slate-50 group-hover:scale-110 transition-transform">
        {icon}
      </div>
      <span className="text-gray-700 font-bold text-xs tracking-widest uppercase">{title}</span>
    </button>
  );

  const esAdmin = usuario.rol === 'ADMIN';

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans antialiased">
      <Toaster
        position="top-right"
        containerStyle={{ top: 65 }}
      />

      <Topbar rol={usuario.rol} />

      <div className="flex-1 flex flex-col items-center justify-center p-8">
        <div className={`grid gap-8 w-full ${esAdmin ? 'max-w-4xl grid-cols-2' : 'max-w-2xl grid-cols-2'}`}>
          <MenuCard
            icon={<Lock className="w-12 h-12 text-orange-400" />}
            title="APERTURA Y CIERRE DE CAJA"
            onClick={() => navigate('/cierre-caja')}
          />
          <MenuCard
            icon={<MonitorSmartphone className="w-12 h-12 text-blue-500" />}
            title="CAJA (TPV)"
            onClick={() => navigate('/caja')}
          />
          {esAdmin && (
            <>
              <MenuCard
                icon={<Package className="w-12 h-12 text-slate-600" />}
                title="INVENTARIO"
                onClick={() => navigate('/inventario')}
              />
              <MenuCard
                icon={<Users className="w-12 h-12 text-teal-600" />}
                title="USUARIOS"
                onClick={() => navigate('/usuarios')}
              />
            </>
          )}
        </div>

        <button
          onClick={handleLogout}
          className="mt-10 flex items-center gap-3 px-6 py-3 bg-white border border-red-100 text-red-500 rounded-2xl font-bold text-[10px] tracking-widest uppercase hover:bg-red-50 transition-all shadow-sm active:scale-95"
        >
          <LogOut size={18} />
          Cerrar Sesión
        </button>
      </div>
    </div>
  );
}