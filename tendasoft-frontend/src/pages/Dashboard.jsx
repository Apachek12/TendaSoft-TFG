import React from 'react';
import Topbar from './Topbar';
import { Lock, MonitorSmartphone, Package, Users } from 'lucide-react';

export default function Dashboard() {
  // Placeholder
  const usuarioActual = { nombre: 'Admin', rol: 'ADMIN' };

  // Componente reutilizable para los botones gigantes
  const MenuCard = ({ icon, title, onClick }) => (
    <button
      onClick={onClick}
      className="bg-white flex flex-col items-center justify-center p-12 rounded-2xl shadow-sm border border-gray-100 hover:shadow-md transition-shadow aspect-[4/3]"
    >
      <div className="mb-6 p-4 rounded-full bg-slate-50">
        {icon}
      </div>
      <span className="text-gray-700 font-bold text-sm tracking-wide">{title}</span>
    </button>
  );

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      <Topbar rol={usuarioActual.rol} />

      {/* Contenedor principal de los botones */}
      <div className="flex-1 flex items-center justify-center p-8">
        <div className={`grid gap-8 w-full max-w-4xl ${usuarioActual.rol === 'ADMIN' ? 'grid-cols-2' : 'grid-cols-2 max-w-2xl'}`}>

          {/* Botones comunes para ambos roles */}
          <MenuCard
            icon={<Lock className="w-10 h-10 text-orange-400" />}
            title="APERTURA Y CIERRE DE CAJA"
            onClick={() => console.log('Ir a Caja Fuerte')}
          />
          <MenuCard
            icon={<MonitorSmartphone className="w-10 h-10 text-blue-500" />}
            title="CAJA"
            onClick={() => console.log('Ir al TPV')}
          />

          {/* Botones exclusivos de Administrador */}
          {usuarioActual.rol === 'ADMIN' && (
            <>
              <MenuCard
                icon={<Package className="w-10 h-10 text-slate-600" />}
                title="INVENTARIO"
                onClick={() => console.log('Ir a Inventario')}
              />
              <MenuCard
                icon={<Users className="w-10 h-10 text-teal-600" />}
                title="USUARIOS"
                onClick={() => console.log('Ir a Usuarios')}
              />
            </>
          )}

        </div>
      </div>
    </div>
  );
}