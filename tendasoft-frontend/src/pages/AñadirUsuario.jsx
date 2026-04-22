import React, { useState } from 'react';
import axios from 'axios';
import { X, ChevronDown } from 'lucide-react';

export default function AñadirUsuario({ isOpen, onClose, onSuccess }) {
  const [loading, setLoading] = useState(false);
  const [nuevoUsuario, setNuevoUsuario] = useState({
    nombreReal: '',
    nombreUsuario: '',
    hashContrasena: '', // Usamos este nombre para que coincida con tu Postman y Java
    rol: 'VENDEDOR'
  });

  const handleGuardar = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      // Enviamos el objeto tal cual lo probaste en Postman
      await axios.post('http://localhost:8080/api/usuarios', nuevoUsuario);
      onSuccess();
      setNuevoUsuario({ nombreReal: '', nombreUsuario: '', hashContrasena: '', rol: 'VENDEDOR' });
      onClose();
    } catch (err) {
      alert("Error al guardar: el nombre de usuario ya existe o el servidor falló.");
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-[#001D3D]/40 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-[24px] w-full max-w-md shadow-2xl animate-in zoom-in-95 duration-200 overflow-hidden">

        {/* Cabecera con Ñ */}
        <div className="flex justify-between items-center p-8 pb-4">
          <h2 className="text-[#001D3D] text-2xl font-black tracking-tight">Dar de alta nuevo usuario</h2>
          <button onClick={onClose} className="p-2 hover:bg-gray-100 rounded-full transition-colors text-slate-400">
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleGuardar} className="p-8 pt-2 space-y-4">
          <input
            required
            className="w-full h-14 px-5 bg-[#F4F7F9] rounded-2xl border-2 border-transparent focus:border-[#00796B] outline-none font-bold text-[#2C3E50] transition-all"
            placeholder="Nombre completo"
            value={nuevoUsuario.nombreReal}
            onChange={(e) => setNuevoUsuario({...nuevoUsuario, nombreReal: e.target.value})}
          />

          <input
            required
            className="w-full h-14 px-5 bg-[#F4F7F9] rounded-2xl border-2 border-transparent focus:border-[#00796B] outline-none font-bold text-[#2C3E50]"
            placeholder="Usuario"
            value={nuevoUsuario.nombreUsuario}
            onChange={(e) => setNuevoUsuario({...nuevoUsuario, nombreUsuario: e.target.value})}
          />

          <input
            required
            type="password"
            className="w-full h-14 px-5 bg-[#F4F7F9] rounded-2xl border-2 border-transparent focus:border-[#00796B] outline-none font-bold text-[#2C3E50]"
            placeholder="Contraseña" // <-- CON Ñ
            value={nuevoUsuario.hashContrasena} // <-- CORREGIDO: ahora coincide con el estado
            onChange={(e) => setNuevoUsuario({...nuevoUsuario, hashContrasena: e.target.value})}
          />

          <div className="relative">
            <select
              className="w-full h-14 px-5 bg-[#F4F7F9] rounded-2xl outline-none appearance-none font-bold text-[#2C3E50] cursor-pointer"
              value={nuevoUsuario.rol}
              onChange={(e) => setNuevoUsuario({...nuevoUsuario, rol: e.target.value})}
            >
              <option value="VENDEDOR">ROL: VENDEDOR</option>
              <option value="ADMIN">ROL: ADMINISTRADOR</option>
            </select>
            <ChevronDown className="absolute right-5 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none" size={20} />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full h-16 bg-[#00796B] hover:bg-[#00695C] text-white font-black rounded-2xl mt-6 transition-all shadow-lg shadow-[#00796B]/20 active:scale-[0.98] uppercase tracking-widest"
          >
            {loading ? 'Guardando...' : 'GUARDAR USUARIO'}
          </button>
        </form>
      </div>
    </div>
  );
}