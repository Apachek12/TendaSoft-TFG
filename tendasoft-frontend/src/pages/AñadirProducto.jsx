import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { X, ChevronDown, Upload, ImageIcon } from 'lucide-react';

export default function AñadirProducto({ isOpen, onClose, onSuccess }) {
  const [loading, setLoading] = useState(false);
  const [categorias, setCategorias] = useState([]);
  const [preview, setPreview] = useState(null); // Para ver la imagen antes de subir
  const [imageFile, setImageFile] = useState(null);

  const [formData, setFormData] = useState({
    nombre: '',
    codigoBarras: '',
    idCategoria: '', // Ajustado a idCategoria
    unidades: '',    // Ajustado a unidades para que coincida con tu lista
    precio: '',
    tipoIva: '21'
  });

  useEffect(() => {
    if (isOpen) {
      axios.get('http://localhost:8080/api/categorias')
        .then(res => setCategorias(res.data))
        .catch(err => console.error("Error al cargar categorías"));
    }
  }, [isOpen]);

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setImageFile(file);
      setPreview(URL.createObjectURL(file)); // Crea una URL temporal para ver la foto
    }
  };

  const handleGuardar = async (e) => {
    e.preventDefault();
    setLoading(true);

    // Usamos FormData para que Spring pueda recibir la imagen y los textos
    const data = new FormData();
    data.append('nombre', nuevoProducto.nombre);
    data.append('codigoBarras', nuevoProducto.codigoBarras);
    data.append('precio', nuevoProducto.precio);
    data.append('unidades', nuevoProducto.unidades);
    data.append('porcentajeIva', nuevoProducto.porcentajeIva);
    data.append('idCategoria', nuevoProducto.idCategoria); // Enviamos el ID

    if (imageFile) {
      data.append('imagen', imageFile); // El archivo binario
    }

    try {
      await axios.post('http://localhost:8080/api/productos', data, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      onSuccess(); // Recarga el inventario
      onClose();
    } catch (err) {
      console.error("Error al guardar:", err.response);
      alert("Error al añadir el producto. Revisa que el código de barras no esté repetido.");
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-[#001D3D]/40 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-[24px] w-full max-w-md shadow-2xl p-8 overflow-hidden">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-[#001D3D] text-2xl font-black tracking-tight">Añadir producto</h2>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-600"><X size={24} /></button>
        </div>

        <form onSubmit={handleGuardar} className="space-y-3">
          <input required className="w-full h-12 px-5 bg-[#F4F7F9] rounded-xl outline-none font-bold text-[#2C3E50]" placeholder="Nombre" value={formData.nombre} onChange={(e) => setFormData({...formData, nombre: e.target.value})} />

          <input className="w-full h-12 px-5 bg-[#F4F7F9] rounded-xl outline-none font-bold text-[#2C3E50]" placeholder="Código de barras" value={formData.codigoBarras} onChange={(e) => setFormData({...formData, codigoBarras: e.target.value})} />

          <div className="relative">
            <select required className="w-full h-12 px-5 bg-[#F4F7F9] rounded-xl outline-none appearance-none font-bold text-[#2C3E50] cursor-pointer" value={formData.idCategoria} onChange={(e) => setFormData({...formData, idCategoria: e.target.value})} >
              <option value="">Selecciona Categoría</option>
              {categorias.map(cat => (
                <option key={cat.idCategoria} value={cat.idCategoria}>{cat.nombre}</option>
              ))}
            </select>
            <ChevronDown className="absolute right-5 top-1/2 -translate-y-1/2 text-[#001D3D]" size={18} />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <input type="number" className="w-full h-12 px-5 bg-[#F4F7F9] rounded-xl outline-none font-bold text-[#2C3E50]" placeholder="Stock" value={formData.unidades} onChange={(e) => setFormData({...formData, unidades: e.target.value})} />
            <input type="number" step="0.01" className="w-full h-12 px-5 bg-[#F4F7F9] rounded-xl outline-none font-bold text-[#2C3E50]" placeholder="Precio" value={formData.precio} onChange={(e) => setFormData({...formData, precio: e.target.value})} />
          </div>

          {/* INPUT DE IMAGEN REAL */}
          <div className="relative h-24 bg-[#F4F7F9] rounded-xl border-2 border-dashed border-gray-200 flex flex-col items-center justify-center overflow-hidden group">
            {preview ? (
              <img src={preview} alt="Vista previa" className="w-full h-full object-cover" />
            ) : (
              <>
                <Upload size={20} className="text-gray-400 group-hover:text-[#00796B]" />
                <span className="text-[10px] font-bold text-gray-400 uppercase mt-1">Cargar Imagen</span>
              </>
            )}
            <input
              type="file"
              accept="image/*"
              className="absolute inset-0 opacity-0 cursor-pointer"
              onChange={handleImageChange}
            />
          </div>

          <button type="submit" disabled={loading} className="w-full h-16 bg-[#455A64] hover:bg-[#37474F] text-white font-black rounded-xl mt-4 transition-all uppercase">
            {loading ? 'SINCRONIZANDO...' : 'GUARDAR PRODUCTO'}
          </button>
        </form>
      </div>
    </div>
  );
}