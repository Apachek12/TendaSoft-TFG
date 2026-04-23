import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { X, Check } from 'lucide-react';

export default function ModalAñadirUsuario({ isOpen, onClose, onSuccess, usuarioEdit }) {
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    nombreUsuario: '',
    nombreReal: '',
    hashContrasena: '', // CAMBIADO: Ahora coincide con el Backend
    rol: 'VENDEDOR'
  });

  useEffect(() => {
    if (isOpen) {
      if (usuarioEdit) {
        setFormData({
          nombreUsuario: usuarioEdit.nombreUsuario || '',
          nombreReal: usuarioEdit.nombreReal || '',
          hashContrasena: '',
          rol: usuarioEdit.rol || 'VENDEDOR'
        });
      } else {
        setFormData({ nombreUsuario: '', nombreReal: '', hashContrasena: '', rol: 'VENDEDOR' });
      }
    }
  }, [isOpen, usuarioEdit]);

  const handleGuardar = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const id = usuarioEdit?.idUsuario || usuarioEdit?.id;

      if (usuarioEdit) {
        // ACTUALIZAR (PUT)
        const payload = { ...formData };
        if (!payload.hashContrasena) delete payload.hashContrasena; // No sobreescribir si está vacío
        await axios.put(`http://localhost:8080/api/usuarios/${id}`, payload);
      } else {
        // CREAR (POST)
        await axios.post('http://localhost:8080/api/usuarios', formData);
      }
      onSuccess();
      onClose();
    } catch (err) {
      console.error("Error:", err.response?.data);
      alert("Error al guardar: " + (err.response?.data?.message || "Revisa los campos"));
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-[#001D3D]/40 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-[24px] w-full max-w-md shadow-2xl overflow-hidden border border-gray-100 animate-in zoom-in-95 duration-200">

        <div className="p-6 flex justify-between items-center bg-white border-b border-gray-50">
          <h2 className="text-[#001D3D] text-2xl font-black tracking-tight">
            {usuarioEdit ? 'Editar Usuario' : 'Nuevo Usuario'}
          </h2>
          <button onClick={onClose} className="p-2 hover:bg-gray-100 rounded-full transition-colors">
            <X size={24} className="text-[#001D3D]" />
          </button>
        </div>

        <form onSubmit={handleGuardar} className="p-8 space-y-4">

          <div className="space-y-1">
            <label className="text-[10px] font-black text-[#95A5A6] uppercase ml-2">Nombre Real</label>
            <input
              required
              className="w-full h-14 px-5 bg-[#F8F9FA] rounded-2xl outline-none font-bold text-[#2C3E50] border-2 border-transparent focus:border-[#00796B] transition-all"
              placeholder="Nombre completo"
              value={formData.nombreReal}
              onChange={(e) => setFormData({...formData, nombreReal: e.target.value})}
            />
          </div>

          <div className="space-y-1">
            <label className="text-[10px] font-black text-[#95A5A6] uppercase ml-2">Nombre de Usuario (@)</label>
            <input
              required
              className="w-full h-14 px-5 bg-[#F8F9FA] rounded-2xl outline-none font-bold text-[#2C3E50] border-2 border-transparent focus:border-[#00796B] transition-all"
              placeholder="ID de acceso"
              value={formData.nombreUsuario}
              onChange={(e) => setFormData({...formData, nombreUsuario: e.target.value})}
            />
          </div>

          <div className="space-y-1">
            <label className="text-[10px] font-black text-[#95A5A6] uppercase ml-2">Contraseña</label>
            <input
              required={!usuarioEdit}
              type="password"
              className="w-full h-14 px-5 bg-[#F8F9FA] rounded-2xl outline-none font-bold text-[#2C3E50] border-2 border-transparent focus:border-[#00796B] transition-all"
              placeholder={usuarioEdit ? "Dejar en blanco para no cambiar" : "Mínimo 6 caracteres"}
              value={formData.hashContrasena}
              onChange={(e) => setFormData({...formData, hashContrasena: e.target.value})}
            />
          </div>

          <div className="space-y-1">
            <label className="text-[10px] font-black text-[#95A5A6] uppercase ml-2">Rol</label>
            <select
              className="w-full h-14 px-5 bg-[#F8F9FA] rounded-2xl outline-none font-bold text-[#2C3E50] border-2 border-transparent focus:border-[#00796B] transition-all cursor-pointer appearance-none"
              value={formData.rol}
              onChange={(e) => setFormData({...formData, rol: e.target.value})}
            >
              <option value="VENDEDOR">VENDEDOR</option>
              <option value="ADMIN">ADMINISTRADOR</option>
            </select>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full h-16 bg-[#00796B] text-white font-black rounded-2xl mt-6 hover:bg-[#004D40] active:scale-[0.98] transition-all flex items-center justify-center gap-3"
          >
            {loading ? 'Guardando...' : <><Check size={24} /> {usuarioEdit ? 'GUARDAR CAMBIOS' : 'CREAR USUARIO'}</>}
          </button>
        </form>
      </div>
    </div>
  );
}