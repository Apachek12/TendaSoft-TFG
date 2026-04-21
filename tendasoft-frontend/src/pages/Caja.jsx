import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { ChevronLeft, ChevronRight, Image as ImageIcon, Delete } from 'lucide-react';

export default function TPVScreen() {
  const [showProducts, setShowProducts] = useState(false);
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);

  // 1. ELIMINADOS LOS PRODUCTOS DE PRUEBA (Iniciamos vacío)
  const [transactionItems, setTransactionItems] = useState([]);
  const [selectedCategoryId, setSelectedCategoryId] = useState('todos');

  useEffect(() => {
    async function fetchData() {
      try {
        setLoading(true);
        const [productsRes, categoriesRes] = await Promise.all([
          axios.get('http://localhost:8080/api/productos'),
          axios.get('http://localhost:8080/api/categorias')
        ]);
        setProducts(productsRes.data);
        setCategories(categoriesRes.data);
      } catch (err) {
        console.error("Error cargando datos del TPV", err);
      } finally {
        setLoading(false);
      }
    }
    fetchData();
  }, []);

  // 2. FUNCIÓN PARA AÑADIR PRODUCTOS A LA VENTA
  const addToSale = (product) => {
    setTransactionItems(prevItems => {
      // Comprobamos si el producto ya está en el carrito usando codigoBarras
      const existingItem = prevItems.find(item => item.codigoBarras === product.codigoBarras);

      if (existingItem) {
        // Si ya existe, creamos un nuevo array con la cantidad incrementada
        return prevItems.map(item =>
          item.codigoBarras === product.codigoBarras
            ? { ...item, quantity: item.quantity + 1 }
            : item
        );
      } else {
        // Si es nuevo, lo añadimos con cantidad 1
        return [...prevItems, {
          codigoBarras: product.codigoBarras,
          name: product.nombre,
          price: product.precio,
          quantity: 1
        }];
      }
    });
  };

  const total = transactionItems.reduce((sum, item) => sum + (item.quantity * item.price), 0);

  // Filtrado usando el campo Categoriasid de tu base de datos
  const filteredProducts = selectedCategoryId === 'todos'
    ? products
    : products.filter(p => p.categoria?.id === selectedCategoryId);

  return (
    <div className="min-h-screen bg-slate-50 flex p-6 font-sans antialiased text-slate-800">

      {/* SECCIÓN IZQUIERDA: Detalle de la Venta */}
      <div className="w-1/2 bg-white rounded-3xl shadow-sm border border-gray-100 flex flex-col mr-6 overflow-hidden">
        <div className="flex p-6 border-b border-gray-100 text-xs font-bold text-slate-400 uppercase tracking-wider">
          <div className="w-2/3">PRODUCTO</div>
          <div className="w-1/6 text-center">CANT.</div>
          <div className="w-1/6 text-right">PRECIO</div>
        </div>

        <div className="flex-1 overflow-y-auto p-3 space-y-2">
          {transactionItems.length === 0 ? (
            <div className="h-full flex flex-col items-center justify-center text-slate-300">
              <p className="text-lg font-medium">No hay productos en la venta</p>
              <p className="text-sm">Escanea un código o selecciona de la lista</p>
            </div>
          ) : (
            transactionItems.map(item => (
              <div key={item.codigoBarras} className="flex p-4 rounded-xl hover:bg-slate-50 items-center animate-in fade-in slide-in-from-left-2">
                <div className="w-2/3">
                  <p className="font-semibold text-slate-800">{item.name}</p>
                  <p className="text-sm text-slate-400">{item.price.toFixed(2)} €</p>
                </div>
                <div className="w-1/6 text-center font-bold text-slate-700">
                  x{item.quantity}
                </div>
                <div className="w-1/6 text-right font-bold text-slate-800">
                  {(item.quantity * item.price).toFixed(2)} €
                </div>
              </div>
            ))
          )}
        </div>

        <div className="p-6 border-t border-gray-100 bg-slate-50 flex items-center justify-between">
          <button
            disabled={transactionItems.length === 0}
            className="bg-emerald-500 hover:bg-emerald-600 disabled:bg-slate-200 text-white font-extrabold px-12 py-3.5 rounded-xl text-lg tracking-wide transition-all shadow-md"
          >
            COBRAR
          </button>
          <div className="text-right">
            <p className="text-xs text-slate-400 font-medium">TOTAL</p>
            <p className="text-3xl font-bold text-slate-800">{total.toFixed(2)} €</p>
          </div>
        </div>
      </div>

      {/* SECCIÓN DERECHA: Catálogo / Teclado */}
      <div className="w-1/2 flex flex-col">
        <input
          type="text"
          placeholder="Código de barras"
          className="w-full p-5 bg-white border border-gray-100 rounded-xl shadow-sm mb-4 focus:outline-none focus:ring-2 focus:ring-slate-300"
        />

        <button
          onClick={() => setShowProducts(!showProducts)}
          className="w-full p-5 bg-slate-700 hover:bg-slate-800 text-white font-bold rounded-xl mb-6 text-sm uppercase tracking-wider transition-colors"
        >
          {showProducts ? 'OCULTAR PRODUCTOS' : 'VER PRODUCTOS'}
        </button>

        <div className="flex-1">
          {showProducts ? (
            <div className="flex flex-col h-full">
              {/* Categorías */}
              <div className="flex items-center space-x-2 bg-white p-3 rounded-xl border border-gray-100 shadow-sm mb-4 overflow-x-auto scrollbar-hide">
                <button
                  onClick={() => setSelectedCategoryId('todos')}
                  className={`px-6 py-2 rounded-lg text-sm font-bold transition-colors whitespace-nowrap ${selectedCategoryId === 'todos' ? 'bg-slate-700 text-white' : 'bg-slate-100 text-slate-600'}`}
                >
                  Todos
                </button>
                {categories.map(cat => (
                  <button
                    key={cat.id}
                    onClick={() => setSelectedCategoryId(cat.id)}
                    className={`px-6 py-2 rounded-lg text-sm font-bold transition-colors whitespace-nowrap ${selectedCategoryId === cat.id ? 'bg-slate-700 text-white' : 'bg-slate-100 text-slate-600'}`}
                  >
                    {cat.nombre}
                  </button>
                ))}
              </div>

              {/* Grid de Productos - ACCIÓN AL HACER CLIC */}
              <div className="flex-1 overflow-y-auto grid grid-cols-3 gap-4 pb-4">
                {filteredProducts.map(prod => (
                  <div
                    key={prod.codigoBarras}
                    onClick={() => addToSale(prod)} // <-- AQUÍ SE AÑADE A LA VENTA
                    className="bg-white p-4 rounded-2xl border border-gray-100 shadow-sm flex flex-col items-center justify-center text-center cursor-pointer hover:border-emerald-400 hover:shadow-md transition-all group aspect-square active:scale-95"
                  >
                    <div className="w-16 h-16 bg-slate-50 rounded-full flex items-center justify-center mb-4 group-hover:bg-emerald-50 transition-colors">
                      <ImageIcon className="w-8 h-8 text-slate-300 group-hover:text-emerald-400" />
                    </div>
                    <p className="text-sm font-semibold text-slate-800 leading-tight mb-1">{prod.nombre}</p>
                    <p className="font-bold text-slate-800">{prod.precio.toFixed(2)} €</p>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            /* Teclado Numérico */
            <div className="flex flex-col h-full">
              <div className="grid grid-cols-3 gap-3 flex-1 mb-4">
                {[1, 2, 3, 4, 5, 6, 7, 8, 9, ',', 0].map(val => (
                  <button
                    key={val}
                    className="bg-white hover:bg-slate-100 text-slate-800 text-4xl font-bold flex items-center justify-center rounded-2xl border border-gray-100 shadow-sm transition-transform active:scale-90"
                  >
                    {val}
                  </button>
                ))}
                <button className="bg-white hover:bg-red-50 text-slate-800 flex items-center justify-center rounded-2xl border border-gray-100 shadow-sm">
                  <Delete className="w-10 h-10 text-slate-400" />
                </button>
              </div>
              <button className="w-full p-6 bg-slate-700 hover:bg-slate-800 text-white font-extrabold rounded-2xl text-2xl tracking-wider shadow-md active:scale-95 transition-all">
                ENTER
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}