import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { X, ChevronDown, Upload, Percent } from 'lucide-react';

const API = import.meta.env.VITE_API_URL;

const TIPOS_IVA = [
  { label: 'IVA General (21%)',       value: '21.00' },
  { label: 'IVA Reducido (10%)',      value: '10.00' },
  { label: 'IVA Superreducido (4%)', value: '4.00'  },
  { label: 'Exento (0%)',             value: '0.00'  }
];

export default function AñadirProducto({ isOpen, onClose, onSuccess, productoEdit }) {
  const [loading, setLoading] = useState(false);
  const [categorias, setCategorias] = useState([]);
  const [preview, setPreview] = useState(null);
  const [imageFile, setImageFile] = useState(null);
  const [formData, setFormData] = useState({
    nombre: '',
    codigoBarras: '',
    idCategoria: '',
    unidades: '',
    precio: '',
    porcentajeIva: '21.00'
  });

  useEffect(() => {
    if (!isOpen) return;
    axios.get(`${API}/api/categorias`)
      .then(res => setCategorias(res.data))
      .catch(err => console.error('Error cargando categorías:', err));
    if (productoEdit) {
      const catId = productoEdit.categoria?.id || productoEdit.categoria?.idCategoria || '';
      setFormData({
        nombre:        productoEdit.nombre || '',
        codigoBarras:  productoEdit.codigoBarras || '',
        idCategoria:   String(catId),
        unidades:      productoEdit.unidades || 0,
        precio:        productoEdit.precio || 0,
        porcentajeIva: productoEdit.porcentajeIva
          ? Number(productoEdit.porcentajeIva).toFixed(2)
          : '21.00'
      });
    } else {
      setFormData({ nombre: '', codigoBarras: '', idCategoria: '', unidades: '', precio: '', porcentajeIva: '21.00' });
      setPreview(null);
      setImageFile(null);
    }
  }, [isOpen, productoEdit]);

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setImageFile(file);
      setPreview(URL.createObjectURL(file));
    }
  };

  const handleGuardar = async (e) => {
    e.preventDefault();
    setLoading(true);
    const data = new FormData();
    Object.entries(formData).forEach(([key, val]) => data.append(key, val));
    if (imageFile) data.append('imagen', imageFile);
    try {
      if (productoEdit) {
        await axios.put(`${API}/api/productos/${productoEdit.codigoBarras}`, data, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
      } else {
        await axios.post(`${API}/api/productos`, data, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
      }
      onSuccess();
      onClose();
    } catch (err) {
      console.error('Error al guardar producto:', err);
      alert('Error al guardar el producto. Revisa los datos.');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-[#001D3D]/40 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-[24px] w-full max-w-md shadow-2xl p-8 overflow-hidden">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-[#001D3D] text-2xl font-black tracking-tight">
            {productoEdit ? 'Editar Producto' : 'Nuevo Producto'}
          </h2>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-600">
            <X size={24} />
          </button>
        </div>
        <form onSubmit={handleGuardar} className="space-y-3">
          <input
            required
            className="w-full h-12 px-5 bg-[#F4F7F9] rounded-xl outline-none font-bold text-[#2C3E50]"
            placeholder="Nombre del producto"
            value={formData.nombre}
            onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
          />
          <input
            required
            readOnly={!!productoEdit}
            className={`w-full h-12 px-5 rounded-xl outline-none font-bold text-[#2C3E50] ${
              productoEdit ? 'bg-slate-200 text-slate-400 cursor-not-allowed' : 'bg-[#F4F7F9]'
            }`}
            placeholder="Código de barras"
            value={formData.codigoBarras}
            onChange={(e) => setFormData({ ...formData, codigoBarras: e.target.value })}
          />
          <div className="relative">
            <select
              required
              className="w-full h-12 px-5 bg-[#F4F7F9] rounded-xl outline-none appearance-none font-bold text-[#2C3E50] cursor-pointer"
              value={formData.idCategoria}
              onChange={(e) => setFormData({ ...formData, idCategoria: e.target.value })}
            >
              <option value="">Selecciona categoría</option>
              {categorias.map(cat => {
                const id = String(cat.id || cat.idCategoria);
                return <option key={id} value={id}>{cat.nombre}</option>;
              })}
            </select>
            <ChevronDown className="absolute right-5 top-1/2 -translate-y-1/2 text-[#001D3D] pointer-events-none" size={18} />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <input
              required
              type="number"
              className="w-full h-12 px-5 bg-[#F4F7F9] rounded-xl outline-none font-bold text-[#2C3E50]"
              placeholder="Stock"
              value={formData.unidades}
              onChange={(e) => setFormData({ ...formData, unidades: e.target.value })}
            />
            <div className="relative">
              <input
                required
                type="number"
                step="0.01"
                className="w-full h-12 px-5 bg-[#F4F7F9] rounded-xl outline-none font-bold text-[#2C3E50]"
                placeholder="Precio PVP"
                value={formData.precio}
                onChange={(e) => setFormData({ ...formData, precio: e.target.value })}
              />
              <span className="absolute right-4 top-1/2 -translate-y-1/2 font-bold text-slate-400">€</span>
            </div>
          </div>
          <div className="relative">
            <select
              required
              className="w-full h-12 px-11 bg-[#F4F7F9] rounded-xl outline-none appearance-none font-bold text-[#2C3E50] cursor-pointer"
              value={formData.porcentajeIva}
              onChange={(e) => setFormData({ ...formData, porcentajeIva: e.target.value })}
            >
              {TIPOS_IVA.map(t => (
                <option key={t.value} value={t.value}>{t.label}</option>
              ))}
            </select>
            <Percent className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none" size={18} />
            <ChevronDown className="absolute right-5 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none" size={18} />
          </div>
          <div className="relative h-24 bg-[#F4F7F9] rounded-xl border-2 border-dashed border-gray-200 flex flex-col items-center justify-center overflow-hidden group transition-colors hover:border-teal-500">
            {preview ? (
              <img src={preview} alt="Vista previa" className="w-full h-full object-cover" />
            ) : (
              <>
                <Upload size={20} className="text-gray-400 group-hover:text-teal-600" />
                <span className="text-[10px] font-bold text-gray-400 uppercase mt-1 group-hover:text-teal-600">
                  {productoEdit ? 'Cambiar imagen' : 'Cargar imagen'}
                </span>
              </>
            )}
            <input type="file" accept="image/*" className="absolute inset-0 opacity-0 cursor-pointer" onChange={handleImageChange} />
          </div>
          <button
            type="submit"
            disabled={loading}
            className="w-full h-16 bg-[#2C3E50] hover:bg-[#1a252f] text-white font-black rounded-xl mt-4 transition-all uppercase shadow-lg shadow-slate-200"
          >
            {loading ? 'Guardando...' : (productoEdit ? 'Actualizar Producto' : 'Guardar Producto')}
          </button>
        </form>
      </div>
    </div>
  );
}