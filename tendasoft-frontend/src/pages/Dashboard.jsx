import React, { useState, useEffect } from 'react';
import Topbar from './Topbar';
import { useNavigate } from 'react-router-dom';
import { Lock, MonitorSmartphone, Package, Users } from 'lucide-react';

export default function Dashboard() {
  const navigate = useNavigate();
  const [usuario, setUsuario] = useState(null);

  useEffect(() => {
    // Recuperamos el usuario del almacenamiento local
    const datosGuardados = localStorage.getItem('usuarioTendaSoft');
    if (datosGuardados) {
      setUsuario(JSON.parse(datosGuardados));
    } else {
      // Si no hay usuario, redirigimos al login por seguridad
      navigate('/');
    }
  }, [navigate]);

  // Si aún está cargando el usuario, no renderizamos nada
  if (!usuario) return null;

  // Componente reutilizable para los botones gigantes
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
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans">
      <Topbar rol={usuario.rol} />

      {/* Contenedor principal de los botones */}
      <div className="flex-1 flex items-center justify-center p-8">
        <div className={`grid gap-8 w-full ${esAdmin ? 'max-w-4xl grid-cols-2' : 'max-w-2xl grid-cols-2'}`}>

          {/* Botones comunes para VENDEDOR y ADMIN */}
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

          {/* Botones exclusivos de Administrador */}
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
      </div>
    </div>
  );
}