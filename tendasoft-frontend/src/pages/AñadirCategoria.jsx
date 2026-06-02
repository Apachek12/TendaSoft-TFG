import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { X, ChevronDown } from 'lucide-react';

const API = import.meta.env.VITE_API_URL;

export default function AñadirCategoria({ isOpen, onClose, onSuccess, categoriaEdit }) {
  const [loading, setLoading] = useState(false);
  const [categoriasPadre, setCategoriasPadre] = useState([]);
  const [formData, setFormData] = useState({
    nombre: '',
    esSubcategoria: false,
    padreId: ''
  });

  useEffect(() => {
    if (!isOpen) return;
    axios.get(`${API}/api/categorias`)
      .then(res => setCategoriasPadre(res.data))
      .catch(err => console.error('Error cargando categorías:', err));
    if (categoriaEdit) {
      setFormData({
        nombre: categoriaEdit.nombre || '',
        esSubcategoria: !!categoriaEdit.categoriaPadre,
        padreId: categoriaEdit.categoriaPadre?.id || categoriaEdit.categoriaPadre?.idCategoria || ''
      });
    } else {
      setFormData({ nombre: '', esSubcategoria: false, padreId: '' });
    }
  }, [isOpen, categoriaEdit]);

  const handleGuardar = async (e) => {
    e.preventDefault();
    setLoading(true);
    const payload = {
      nombre: formData.nombre,
      orden: 1,
      categoriaPadre: formData.esSubcategoria && formData.padreId
        ? { id: parseInt(formData.padreId) }
        : null
    };
    try {
      if (categoriaEdit) {
        const id = categoriaEdit.id || categoriaEdit.idCategoria;
        await axios.put(`${API}/api/categorias/${id}`, payload);
      } else {
        await axios.post(`${API}/api/categorias`, payload);
      }
      onSuccess();
      onClose();
    } catch (err) {
      console.error('Error al guardar categoría:', err);
      alert('No se pudo guardar la categoría.');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  const editId = categoriaEdit?.id || categoriaEdit?.idCategoria;

  return (
    <div className="fixed inset-0 bg-[#001D3D]/40 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-[24px] w-full max-w-md shadow-2xl animate-in zoom-in-95 duration-200 p-8">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-[#001D3D] text-2xl font-black tracking-tight">
            {categoriaEdit ? 'Editar categoría' : 'Añadir categoría'}
          </h2>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-600">
            <X size={24} />
          </button>
        </div>
        <form onSubmit={handleGuardar} className="space-y-5">
          <input
            required
            className="w-full h-14 px-5 bg-[#F4F7F9] rounded-2xl border-none outline-none font-bold text-[#2C3E50]"
            placeholder="Nombre"
            value={formData.nombre}
            onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
          />
          <div className="flex items-center space-x-3 px-2">
            <span className="text-[#001D3D] font-bold">Subcategoría</span>
            <button
              type="button"
              onClick={() => setFormData({ ...formData, esSubcategoria: !formData.esSubcategoria })}
              className={`w-12 h-6 rounded-full transition-all relative ${formData.esSubcategoria ? 'bg-[#001D3D]' : 'bg-gray-300'}`}
            >
              <div className={`absolute top-1 w-4 h-4 bg-white rounded-full transition-all ${formData.esSubcategoria ? 'left-7' : 'left-1'}`} />
            </button>
          </div>
          {formData.esSubcategoria && (
            <div className="relative animate-in fade-in slide-in-from-top-2">
              <select
                className="w-full h-14 px-5 bg-[#F4F7F9] rounded-2xl outline-none appearance-none font-bold text-[#2C3E50] cursor-pointer"
                value={formData.padreId}
                onChange={(e) => setFormData({ ...formData, padreId: e.target.value })}
              >
                <option value="">Categoría padre</option>
                {categoriasPadre.map(cat => {
                  const catId = cat.id || cat.idCategoria;
                  if (categoriaEdit && catId === editId) return null;
                  return <option key={catId} value={catId}>{cat.nombre}</option>;
                })}
              </select>
              <ChevronDown className="absolute right-5 top-1/2 -translate-y-1/2 text-[#001D3D] pointer-events-none" size={20} />
            </div>
          )}
          <button
            type="submit"
            disabled={loading}
            className="w-full h-16 border-2 border-[#001D3D] text-[#001D3D] font-black rounded-2xl hover:bg-[#001D3D] hover:text-white transition-all uppercase tracking-widest mt-4 shadow-lg shadow-slate-200"
          >
            {loading ? 'GUARDANDO...' : (categoriaEdit ? 'ACTUALIZAR CATEGORÍA' : 'GUARDAR CATEGORÍA')}
          </button>
        </form>
      </div>
    </div>
  );
}