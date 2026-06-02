
import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { X, Search, Check, Image as ImageIcon, CornerDownRight } from 'lucide-react';

const API = import.meta.env.VITE_API_URL;

export default function ModalImportarProductos({ isOpen, onClose, onSuccess, categoriaEdit }) {
  const [loading, setLoading]                         = useState(false);
  const [busqueda, setBusqueda]                       = useState('');
  const [productosDisponibles, setProductosDisponibles] = useState([]);
  const [seleccionados, setSeleccionados]             = useState({});

  useEffect(() => {
    if (!isOpen || !categoriaEdit) return;
    setBusqueda('');
    setSeleccionados({});
    cargarProductosDisponibles();
  }, [isOpen, categoriaEdit]);

  // Carga todos los productos y filtra los que ya pertenecen a esta categoría
  const cargarProductosDisponibles = async () => {
    try {
      const { data } = await axios.get(`${API}/api/productos`);
      const yaAsignados = new Set(categoriaEdit.productos?.map(p => p.codigoBarras));
      setProductosDisponibles(data.filter(p => !yaAsignados.has(p.codigoBarras)));
    } catch (err) {
      console.error('Error cargando productos disponibles:', err);
    }
  };

  const toggleSeleccion = (codigoBarras) => {
    setSeleccionados(prev => ({ ...prev, [codigoBarras]: !prev[codigoBarras] }));
  };

  const handleImportar = async () => {
    const codigos = Object.keys(seleccionados).filter(cb => seleccionados[cb]);
    if (codigos.length === 0) return;

    setLoading(true);
    const idCat = categoriaEdit.id || categoriaEdit.idCategoria;
    try {
      await axios.post(`${API}/api/categorias/${idCat}/importar-productos`, { codigosBarras: codigos });
      onSuccess();
      onClose();
    } catch (err) {
      console.error('Error importando productos:', err);
      alert('Hubo un error al importar los productos seleccionados.');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen || !categoriaEdit) return null;

  const productosFiltrados = productosDisponibles.filter(p =>
    p.nombre.toLowerCase().includes(busqueda.toLowerCase()) ||
    p.codigoBarras.includes(busqueda)
  );

  const totalSeleccionados = Object.values(seleccionados).filter(Boolean).length;

  return (
    <div className="fixed inset-0 bg-[#001D3D]/40 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-[24px] w-full max-w-2xl h-[90vh] shadow-2xl animate-in zoom-in-95 duration-200 overflow-hidden flex flex-col border border-gray-100">
        <div className="p-6 flex justify-between items-center bg-white border-b border-gray-50 flex-shrink-0">
          <div className="flex items-center gap-3">
            <CornerDownRight className="w-8 h-8 text-[#001D3D] p-1.5 bg-slate-100 rounded-xl" />
            <div>
              <h2 className="text-[#001D3D] text-2xl font-black tracking-tight">Importar productos</h2>
              <p className="text-sm font-bold text-slate-400 mt-1">
                Añadir a: <span className="text-[#1976D2]">{categoriaEdit.nombre}</span>
              </p>
            </div>
          </div>
          <button onClick={onClose} className="p-2 hover:bg-gray-100 rounded-full transition-colors">
            <X size={24} className="text-slate-400 hover:text-slate-600" />
          </button>
        </div>
        <div className="p-4 border-b border-gray-50 bg-white flex-shrink-0 relative">
          <Search className="absolute left-7 top-1/2 -translate-y-1/2 text-slate-400 w-5 h-5" />
          <input
            type="text"
            placeholder="Buscar por nombre o código de barras..."
            value={busqueda}
            onChange={e => setBusqueda(e.target.value)}
            className="w-full h-12 pl-14 pr-4 bg-[#F8F9FA] rounded-xl outline-none font-bold text-[#2C3E50]"
          />
        </div>
        <div className="flex-1 overflow-y-auto divide-y divide-slate-50 p-4">
          {productosFiltrados.length > 0 ? productosFiltrados.map(p => {
            const isSelected = !!seleccionados[p.codigoBarras];
            return (
              <div
                key={p.codigoBarras}
                onClick={() => toggleSeleccion(p.codigoBarras)}
                className={`flex items-center justify-between p-4 cursor-pointer rounded-xl transition-all ${isSelected ? 'bg-[#00796B]/5' : 'hover:bg-slate-50'}`}
              >
                <div className="flex items-center">
                  <div className={`w-6 h-6 rounded-md border-2 flex items-center justify-center mr-4 transition-all ${isSelected ? 'bg-[#00796B] border-[#00796B]' : 'border-slate-300 bg-white'}`}>
                    {isSelected && <Check className="w-4 h-4 text-white" />}
                  </div>
                  <div className="w-10 h-10 rounded-xl flex items-center justify-center mr-4 border border-slate-200 bg-white shadow-sm overflow-hidden shrink-0">
                    {p.urlImagen
                      ? <img src={`${API}/uploads/${p.urlImagen}`} alt={p.nombre} className="w-full h-full object-cover" />
                      : <ImageIcon className="text-slate-300 w-5 h-5" />
                    }
                  </div>
                  <div>
                    <p className="font-bold text-slate-700 text-sm">{p.nombre}</p>
                    <p className="text-xs font-semibold text-slate-400">{p.codigoBarras}</p>
                  </div>
                </div>
                <p className="text-xs font-semibold text-teal-600 border border-teal-100 px-2 py-1 rounded-md bg-teal-50">
                  {p.precio.toFixed(2)} €
                </p>
              </div>
            );
          }) : (
            <p className="p-10 text-center text-slate-400 italic text-sm">
              No hay productos disponibles para importar.
            </p>
          )}
        </div>
        <div className="p-6 border-t border-gray-50 bg-white flex-shrink-0">
          <button
            onClick={handleImportar}
            disabled={loading || totalSeleccionados === 0}
            className="w-full h-16 bg-[#00796B] text-white font-black rounded-2xl hover:bg-[#004D40] active:scale-[0.98] transition-all flex items-center justify-center gap-3 disabled:bg-slate-200 disabled:text-slate-400 shadow-lg shadow-[#00796B]/15"
          >
            {loading ? 'Sincronizando...' : `IMPORTAR ${totalSeleccionados} PRODUCTOS`}
          </button>
        </div>
      </div>
    </div>
  );
}