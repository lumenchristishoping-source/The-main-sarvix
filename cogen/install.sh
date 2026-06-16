#!/data/data/com.termux/files/usr/bin/bash
echo "📦 Installing COGEN Memory Engine..."
mkdir -p $PREFIX/lib/cogen
cp /sdcard/cogen/*.py $PREFIX/lib/cogen/
pip install -q requests
cat > $PREFIX/bin/cogen << 'LAUNCHER'
#!/data/data/com.termux/files/usr/bin/bash
cd $PREFIX/lib/cogen
python cogen.py "$@"
LAUNCHER
cat > $PREFIX/bin/cogen-debug << 'LAUNCHER'
#!/data/data/com.termux/files/usr/bin/bash
cd $PREFIX/lib/cogen
python main.py "$@"
LAUNCHER
chmod +x $PREFIX/bin/cogen $PREFIX/bin/cogen-debug
echo "✅ COGEN installed! Run 'cogen' to start"
