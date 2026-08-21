#!/usr/bin/env python3
from __future__ import annotations
import math
from pathlib import Path
from dataclasses import dataclass
from typing import Iterable
from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = Path(__file__).resolve().parents[1]
JSR_GRAFFITI_URL = 'https://storage.googleapis.com/greg-kennedy.com/jsr/JSRGraffiti.zip'
JSR_GRAFFITI_SHA256 = '8541009fcfb3ec77f22e7aeafb2bcfceebd64decddf168171df24182438c70d9'
OBJ_DIR = ROOT / 'src/main/resources/assets/jetsetcraft/models/obj'
TEX_DIR = ROOT / 'src/main/resources/assets/jetsetcraft/textures/item'
GRAFF_DIR = ROOT / 'src/main/resources/assets/jetsetcraft/textures/graffiti'
OBJ_DIR.mkdir(parents=True, exist_ok=True)
TEX_DIR.mkdir(parents=True, exist_ok=True)
GRAFF_DIR.mkdir(parents=True, exist_ok=True)

@dataclass
class Face:
    mat: str
    idx: tuple[int, ...]

class Mesh:
    def __init__(self, name: str):
        self.name = name
        self.v: list[tuple[float,float,float]] = []
        self.vt: list[tuple[float,float]] = []
        self.faces: list[Face] = []

    def add_vertex(self, p, uv=(0.0,0.0)) -> int:
        self.v.append(tuple(map(float,p)))
        self.vt.append(tuple(map(float,uv)))
        return len(self.v)

    def tri(self, a,b,c, mat, uva=(0,0),uvb=(1,0),uvc=(0.5,1)):
        ids=(self.add_vertex(a,uva), self.add_vertex(b,uvb), self.add_vertex(c,uvc))
        self.faces.append(Face(mat, ids))

    def quad(self, a,b,c,d, mat, uvs=((0,0),(1,0),(1,1),(0,1))):
        ids=tuple(self.add_vertex(p,uv) for p,uv in zip((a,b,c,d),uvs))
        self.faces.append(Face(mat, ids))

    def box(self, center, size, mat):
        cx,cy,cz=center; sx,sy,sz=[s/2 for s in size]
        p=[(cx-sx,cy-sy,cz-sz),(cx+sx,cy-sy,cz-sz),(cx+sx,cy+sy,cz-sz),(cx-sx,cy+sy,cz-sz),
           (cx-sx,cy-sy,cz+sz),(cx+sx,cy-sy,cz+sz),(cx+sx,cy+sy,cz+sz),(cx-sx,cy+sy,cz+sz)]
        for q in [(0,1,2,3),(5,4,7,6),(4,0,3,7),(1,5,6,2),(3,2,6,7),(4,5,1,0)]:
            self.quad(*(p[i] for i in q), mat)

    def cylinder(self, center, radius, length, axis, mat, segments=24):
        cx,cy,cz=center
        def p(t, side):
            ang=t*2*math.pi/segments; c=math.cos(ang)*radius; s=math.sin(ang)*radius
            off=(-length/2 if side==0 else length/2)
            if axis=='x': return (cx+off, cy+c, cz+s)
            if axis=='y': return (cx+c, cy+off, cz+s)
            return (cx+c, cy+s, cz+off)
        for i in range(segments):
            j=(i+1)%segments
            self.quad(p(i,0),p(j,0),p(j,1),p(i,1),mat,((i/segments,0),(j/segments,0),(j/segments,1),(i/segments,1)))
        c0=(cx-length/2,cy,cz) if axis=='x' else ((cx,cy-length/2,cz) if axis=='y' else (cx,cy,cz-length/2))
        c1=(cx+length/2,cy,cz) if axis=='x' else ((cx,cy+length/2,cz) if axis=='y' else (cx,cy,cz+length/2))
        for i in range(segments):
            j=(i+1)%segments
            self.tri(c0,p(j,0),p(i,0),mat)
            self.tri(c1,p(i,1),p(j,1),mat)

    def tube_between(self, a, b, radius, mat, segments=16):
        ax,ay,az=a; bx,by,bz=b
        dx,dy,dz=bx-ax,by-ay,bz-az
        L=math.sqrt(dx*dx+dy*dy+dz*dz)
        if L<1e-6: return
        w=(dx/L,dy/L,dz/L)
        helper=(0,1,0) if abs(w[1])<0.9 else (1,0,0)
        ux=helper[1]*w[2]-helper[2]*w[1]; uy=helper[2]*w[0]-helper[0]*w[2]; uz=helper[0]*w[1]-helper[1]*w[0]
        ul=math.sqrt(ux*ux+uy*uy+uz*uz); u=(ux/ul,uy/ul,uz/ul)
        v=(w[1]*u[2]-w[2]*u[1], w[2]*u[0]-w[0]*u[2], w[0]*u[1]-w[1]*u[0])
        ring=[]
        for end in (a,b):
            rr=[]
            for i in range(segments):
                t=2*math.pi*i/segments
                cs,sn=math.cos(t),math.sin(t)
                rr.append((end[0]+radius*(u[0]*cs+v[0]*sn), end[1]+radius*(u[1]*cs+v[1]*sn), end[2]+radius*(u[2]*cs+v[2]*sn)))
            ring.append(rr)
        for i in range(segments):
            j=(i+1)%segments
            self.quad(ring[0][i],ring[0][j],ring[1][j],ring[1][i],mat,((i/segments,0),(j/segments,0),(j/segments,1),(i/segments,1)))
        for i in range(segments):
            j=(i+1)%segments
            self.tri(a,ring[0][j],ring[0][i],mat)
            self.tri(b,ring[1][i],ring[1][j],mat)

    def torus(self, center, major, minor, mat, major_seg=40, minor_seg=10, plane='yz'):
        cx,cy,cz=center
        def p(i,j):
            a=2*math.pi*i/major_seg; b=2*math.pi*j/minor_seg
            R=major+minor*math.cos(b); q=minor*math.sin(b)
            if plane=='yz': return (cx+q, cy+R*math.sin(a), cz+R*math.cos(a))
            if plane=='xz': return (cx+R*math.cos(a), cy+q, cz+R*math.sin(a))
            return (cx+R*math.cos(a),cy+R*math.sin(a),cz+q)
        for i in range(major_seg):
            ni=(i+1)%major_seg
            for j in range(minor_seg):
                nj=(j+1)%minor_seg
                self.quad(p(i,j),p(ni,j),p(ni,nj),p(i,nj),mat,((i/major_seg,j/minor_seg),(ni/major_seg,j/minor_seg),(ni/major_seg,nj/minor_seg),(i/major_seg,nj/minor_seg)))

    def ellipsoid(self, center, radii, mat, lon=28, lat=14, y_min=-1.0, y_max=1.0):
        cx,cy,cz=center; rx,ry,rz=radii
        rings=[]
        for j in range(lat+1):
            phi=-math.pi/2 + math.pi*j/lat
            yn=math.sin(phi)
            if yn<y_min or yn>y_max: continue
            ring=[]
            for i in range(lon):
                th=2*math.pi*i/lon
                x=cx+rx*math.cos(phi)*math.cos(th); y=cy+ry*yn; z=cz+rz*math.cos(phi)*math.sin(th)
                ring.append((x,y,z))
            rings.append((j,ring))
        for r in range(len(rings)-1):
            j,a=rings[r]; _,b=rings[r+1]
            for i in range(lon):
                ni=(i+1)%lon
                self.quad(a[i],a[ni],b[ni],b[i],mat,((i/lon,j/lat),(ni/lon,j/lat),(ni/lon,(j+1)/lat),(i/lon,(j+1)/lat)))

    def deck(self, length=1.15,width=.34,thickness=.065, mat='deck', segments=40):
        top=[]; bottom=[]
        for i in range(segments):
            t=2*math.pi*i/segments
            # superellipse-like rounded board outline
            x=(width/2)*math.copysign(abs(math.cos(t))**0.55, math.cos(t))
            z=(length/2)*math.copysign(abs(math.sin(t))**0.80, math.sin(t))
            kick=0.055*(abs(z)/(length/2))**7
            top.append((x,0.10+kick,z)); bottom.append((x,0.10+kick-thickness,z))
        ct=(0,0.10,0); cb=(0,0.10-thickness,0)
        for i in range(segments):
            j=(i+1)%segments
            def uv(p): return (p[0]/width+0.5,p[2]/length+0.5)
            self.tri(ct,top[i],top[j],mat,(.5,.5),uv(top[i]),uv(top[j]))
            self.tri(cb,bottom[j],bottom[i],mat,(.5,.5),uv(bottom[j]),uv(bottom[i]))
            self.quad(top[i],top[j],bottom[j],bottom[i],mat)

    def write(self, materials: dict[str,str]):
        obj=OBJ_DIR/f'{self.name}.obj'; mtl=OBJ_DIR/f'{self.name}.mtl'
        with obj.open('w',encoding='utf8') as f:
            f.write(f'# JetSetCraft procedural production mesh: {self.name}\nmtllib {self.name}.mtl\no {self.name}\n')
            for x,y,z in self.v: f.write(f'v {x:.6f} {y:.6f} {z:.6f}\n')
            for u,v in self.vt: f.write(f'vt {u:.6f} {v:.6f}\n')
            current=None
            for face in self.faces:
                if face.mat!=current:
                    f.write(f'usemtl {face.mat}\n'); current=face.mat
                f.write('f '+' '.join(f'{i}/{i}' for i in face.idx)+'\n')
        with mtl.open('w',encoding='utf8') as f:
            f.write(f'# JetSetCraft materials for {self.name}\n')
            for mat,tex in materials.items():
                f.write(f'newmtl {mat}\nmap_Kd jetsetcraft:item/{tex}\nKd 1.000 1.000 1.000\nillum 1\n')
        return obj


def make_skate():
    m=Mesh('inline_skates')
    # high-detail boot upper / sole / cuff
    m.ellipsoid((0,0.23,0.03),(.155,.22,.38),'shell',lon=32,lat=16)
    m.ellipsoid((0,0.24,0.30),(.16,.17,.22),'shell',lon=28,lat=14)
    m.cylinder((0,0.41,-0.20),.155,.24,'y','shell',segments=28)
    m.box((0,0.03,0.03),(.32,.08,.78),'rubber')
    # chassis rails
    m.box((-.09,-.075,0.02),(.045,.085,.72),'metal')
    m.box(( .09,-.075,0.02),(.045,.085,.72),'metal')
    # four wheels with hubs
    for z in (-.27,-.09,.09,.27):
        m.cylinder((0,-.18,z),.075,.215,'x','wheel',segments=28)
        m.cylinder((0,-.18,z),.027,.235,'x','metal',segments=18)
    # cuff buckle, toe protector, heel guard
    m.box((0,0.43,-.06),(.34,.045,.10),'metal')
    m.box((0,0.13,.365),(.30,.12,.07),'rubber')
    m.box((0,0.20,-.35),(.27,.28,.055),'rubber')
    # lace/buckle bars
    for z in (.04,.11,.18): m.box((0,0.35,z),(.31,.025,.032),'metal')
    m.write({'shell':'skate_shell','rubber':'rubber','wheel':'wheel','metal':'metal'})



def make_quad_skate():
    m=Mesh('quad_skates')
    # Sculpted high-top boot with a wider retro derby silhouette.
    m.ellipsoid((0,0.25,0.01),(.17,.23,.37),'shell',lon=34,lat=18)
    m.ellipsoid((0,0.25,0.30),(.17,.16,.23),'shell',lon=30,lat=16)
    m.cylinder((0,0.43,-0.18),.16,.25,'y','shell',segments=30)
    m.box((0,0.035,0.02),(.35,.075,.79),'rubber')
    # Reinforced sole plate and two polished trucks.
    m.box((0,-.035,0.02),(.30,.055,.67),'metal')
    for z in (-.225,.225):
        m.box((0,-.085,z),(.30,.055,.09),'metal')
        m.cylinder((0,-.105,z),.023,.47,'x','metal',segments=22)
        for x in (-.205,.205):
            m.cylinder((x,-.145,z),.082,.07,'x','wheel',segments=30)
            m.cylinder((x,-.145,z),.027,.082,'x','metal',segments=18)
    # Toe stop, heel guard, ankle cuff and three bright buckle/lace bars.
    m.ellipsoid((0,-.085,.405),(.075,.085,.075),'accent',lon=24,lat=12)
    m.box((0,0.20,-.35),(.285,.27,.06),'rubber')
    m.box((0,0.44,-.06),(.35,.04,.12),'accent')
    for z in (.03,.11,.19):
        m.box((0,0.35,z),(.31,.025,.034),'metal')
    # Side plates give the quad skate a strong readable silhouette in third person.
    m.box((-.162,0.08,.015),(.025,.10,.52),'accent')
    m.box(( .162,0.08,.015),(.025,.10,.52),'accent')
    m.write({'shell':'skate_shell','rubber':'rubber','wheel':'wheel','metal':'metal','accent':'accent'})

def make_board():
    m=Mesh('street_board')
    m.deck(mat='deck')
    # trucks and axles
    for z in (-.34,.34):
        m.box((0,0.015,z),(.24,.06,.09),'metal')
        m.cylinder((0,-.005,z),.025,.64,'x','metal',segments=20)
        for x in (-.31,.31):
            m.cylinder((x,-.035,z),.070,.075,'x','wheel',segments=28)
            m.cylinder((x,-.035,z),.025,.082,'x','metal',segments=18)
    # deck rails / edge bumpers
    m.box((0,0.045,0),(.025,.035,.88),'accent')
    m.write({'deck':'deck','metal':'metal','wheel':'wheel','accent':'accent'})


def make_bmx():
    m=Mesh('bmx')
    rear=(0,.34,-.52); front=(0,.34,.56); crank=(0,.36,-.03); seat=(0,.76,-.18); head=(0,.70,.35)
    # wheels - tire and neon rim
    for c in (rear,front):
        m.torus(c,.315,.050,'rubber',major_seg=44,minor_seg=10,plane='yz')
        m.torus(c,.265,.015,'metal',major_seg=44,minor_seg=6,plane='yz')
        m.cylinder(c,.035,.22,'x','metal',segments=20)
        # spokes
        for a in range(0,360,30):
            r=math.radians(a); end=(0,c[1]+.255*math.sin(r),c[2]+.255*math.cos(r))
            m.tube_between(c,end,.007,'metal',segments=8)
    # frame triangle / stays
    tubes=[(rear,crank),(crank,head),(head,seat),(seat,crank),(seat,rear),
           ((-.06,rear[1],rear[2]),(-.06,crank[1],crank[2])),((.06,rear[1],rear[2]),(.06,crank[1],crank[2]))]
    for a,b in tubes: m.tube_between(a,b,.035,'frame',segments=18)
    # fork legs
    m.tube_between((-.055,front[1],front[2]),(-.055,head[1],head[2]),.027,'frame',segments=16)
    m.tube_between(( .055,front[1],front[2]),( .055,head[1],head[2]),.027,'frame',segments=16)
    # handlebar stem + bar + grips
    m.tube_between(head,(0,.96,.39),.025,'metal',segments=16)
    m.tube_between((-.31,.96,.39),(.31,.96,.39),.025,'metal',segments=18)
    m.cylinder((-.315,.96,.39),.035,.10,'x','rubber',segments=20)
    m.cylinder(( .315,.96,.39),.035,.10,'x','rubber',segments=20)
    # seat and post
    m.tube_between(seat,(0,.89,-.20),.025,'metal',segments=16)
    m.ellipsoid((0,.92,-.20),(.13,.045,.19),'rubber',lon=28,lat=12)
    # cranks + pedals
    m.cylinder(crank,.06,.16,'x','metal',segments=22)
    m.tube_between((-.08,crank[1],crank[2]),(-.22,crank[1]-.03,crank[2]+.05),.015,'metal',segments=10)
    m.tube_between(( .08,crank[1],crank[2]),( .22,crank[1]+.03,crank[2]-.05),.015,'metal',segments=10)
    m.box((-.24,crank[1]-.03,crank[2]+.05),(.10,.025,.07),'rubber')
    m.box(( .24,crank[1]+.03,crank[2]-.05),(.10,.025,.07),'rubber')
    m.write({'frame':'bike_frame','metal':'metal','rubber':'rubber'})



def make_spray_can():
    m=Mesh('spray_can')
    # Dense 48-sided body with rolled metal rims, domed shoulder, cap collar and separate actuator/nozzle.
    m.cylinder((0,0.0,0),.24,.96,'y','can',segments=48)
    m.torus((0,-.49,0),.225,.018,'metal',major_seg=48,minor_seg=8,plane='xz')
    m.torus((0,.49,0),.225,.018,'metal',major_seg=48,minor_seg=8,plane='xz')
    m.ellipsoid((0,.49,0),(.235,.12,.235),'metal',lon=36,lat=16,y_min=0.0,y_max=1.0)
    m.cylinder((0,.575,0),.105,.10,'y','cap',segments=36)
    m.cylinder((0,.645,0),.073,.075,'y','nozzle',segments=32)
    # Press button and forward spray outlet.
    m.box((0,.705,0),(.115,.045,.13),'cap')
    m.cylinder((0,.705,.078),.022,.075,'z','nozzle',segments=20)
    # Protective lower bumper and raised label bands add silhouette/detail rather than relying on a flat texture.
    m.torus((0,-.40,0),.242,.010,'cap',major_seg=48,minor_seg=6,plane='xz')
    m.torus((0,.29,0),.242,.010,'cap',major_seg=48,minor_seg=6,plane='xz')
    m.write({'can':'accent','metal':'metal','cap':'rubber','nozzle':'metal'})

def texture_gradient(path:Path, size, stops, noise=0):
    w,h=size; im=Image.new('RGB',size); px=im.load()
    for y in range(h):
        t=y/max(1,h-1); idx=min(len(stops)-2,int(t*(len(stops)-1))); lt=t*(len(stops)-1)-idx
        a=stops[idx]; b=stops[idx+1]
        for x in range(w):
            wig=math.sin((x+y)*.075)*3 if noise else 0
            px[x,y]=tuple(max(0,min(255,int(a[k]*(1-lt)+b[k]*lt+wig))) for k in range(3))
    im.save(path)


def make_textures():
    texture_gradient(TEX_DIR/'skate_shell.png',(256,256),[(16,12,36),(42,20,78),(18,190,205),(245,51,169)],1)
    texture_gradient(TEX_DIR/'bike_frame.png',(256,256),[(10,35,48),(16,194,201),(230,43,160),(255,180,38)],1)
    texture_gradient(TEX_DIR/'metal.png',(128,128),[(55,62,70),(198,214,220),(72,82,92)],1)
    texture_gradient(TEX_DIR/'rubber.png',(128,128),[(10,10,14),(38,40,46),(13,13,17)],1)
    texture_gradient(TEX_DIR/'wheel.png',(128,128),[(22,18,34),(78,34,110),(20,210,220)],1)
    texture_gradient(TEX_DIR/'accent.png',(128,128),[(255,60,180),(255,190,35),(20,220,220)],0)

    # deck: original, busy cel-graffiti pattern with grip/noise details
    im=Image.new('RGB',(512,512),(17,18,24)); d=ImageDraw.Draw(im)
    for i in range(18):
        x=-140+i*42
        d.polygon([(x,512),(x+90,512),(x+330,0),(x+240,0)], fill=(20+(i*17)%130, 50+(i*37)%180, 80+(i*53)%170))
    for r in range(14):
        cx=(r*97)%560-20; cy=(r*151)%560-20; rad=25+(r*13)%70
        d.ellipse((cx-rad,cy-rad,cx+rad,cy+rad),outline=(248,62,180),width=8)
    d.text((28,196),'JET SET',fill=(248,244,220),stroke_width=5,stroke_fill=(25,16,38))
    d.text((68,260),'CRAFT',fill=(255,193,43),stroke_width=6,stroke_fill=(25,16,38))
    im=im.filter(ImageFilter.UnsharpMask(radius=1,percent=140,threshold=2)); im.save(TEX_DIR/'deck.png')


def transparentize_white(im:Image.Image):
    im=im.convert('RGBA'); p=im.load()
    for y in range(im.height):
        for x in range(im.width):
            r,g,b,a=p[x,y]; brightness=(r+g+b)/3
            if brightness>244:
                alpha=max(0,int((255-brightness)*22))
                p[x,y]=(r,g,b,min(a,alpha))
    return im


def place_on_canvas(crop:Image.Image, out:Path):
    crop=transparentize_white(crop)
    bbox=crop.getbbox()
    if bbox: crop=crop.crop(bbox)
    canvas=Image.new('RGBA',(512,320),(0,0,0,0))
    crop.thumbnail((500,300),Image.Resampling.LANCZOS)
    canvas.alpha_composite(crop,((512-crop.width)//2,(320-crop.height)//2))
    canvas.save(out)


def _resource_safe_name(name: str) -> str:
    return ''.join(c.lower() if (c.isalnum() or c in '._-') else '_' for c in name)


def _rgba_copy(src: Path, dst: Path) -> tuple[int, int]:
    """Normalize decoder format only; preserve exact pixel dimensions and visual pixels."""
    with Image.open(src) as im:
        rgba = im.convert('RGBA')
        dst.parent.mkdir(parents=True, exist_ok=True)
        rgba.save(dst, optimize=True)
        return rgba.size


def make_graffiti():
    catalog = []

    # Authorization-backed JSRF sheet derivatives retained from the original prototype.
    sheet=ROOT/'source_assets/authorized/jsrf/graffiti/poison_jam_sheet.png'
    if sheet.exists():
        im=Image.open(sheet)
        crops = (
            ('poison_jam_a', im.crop((0,0,1024,525)), GRAFF_DIR/'tag_0.png'),
            ('poison_jam_b', im.crop((0,455,525,1024)), GRAFF_DIR/'tag_1.png'),
            ('poison_jam_c', im.crop((390,455,1024,1024)), GRAFF_DIR/'tag_2.png'),
        )
        for ident, crop, out in crops:
            place_on_canvas(crop, out)
            with Image.open(out) as tag:
                w, h = tag.size
            catalog.append({'id': ident, 'texture': f'jetsetcraft:textures/graffiti/{out.name}', 'width': w, 'height': h})
    else:
        # Clean-source fallback: three original JetSetCraft tags so a fresh clone never ships blank decals.
        labels=('JET','SET','CRAFT')
        palettes=(((255,55,174,255),(35,225,232,255)),((255,190,40,255),(104,62,242,255)),((45,228,168,255),(255,74,82,255)))
        for i,(label,pal) in enumerate(zip(labels,palettes)):
            tag=Image.new('RGBA',(512,320),(0,0,0,0)); td=ImageDraw.Draw(tag)
            pts=[(28,230),(80,76),(148,155),(218,54),(278,170),(350,62),(474,208)]
            td.line(pts,fill=(12,10,28,255),width=58,joint='curve')
            td.line(pts,fill=pal[0],width=40,joint='curve')
            td.line([(x,y-18) for x,y in pts],fill=pal[1],width=10,joint='curve')
            td.text((150,120),label,fill=(255,245,214,255),stroke_width=5,stroke_fill=(16,10,30,255))
            tag=tag.filter(ImageFilter.UnsharpMask(radius=1.0,percent=180,threshold=2))
            out=GRAFF_DIR/f'tag_{i}.png'; tag.save(out)
            catalog.append({'id': f'fallback_{label.lower()}', 'texture': f'jetsetcraft:textures/graffiti/{out.name}', 'width': 512, 'height': 320})

    # Original JetSetCraft tag.
    im=Image.new('RGBA',(512,320),(0,0,0,0)); d=ImageDraw.Draw(im)
    pts=[(30,210),(75,70),(130,135),(180,55),(235,150),(300,45),(355,135),(445,65),(480,230),(390,190),(320,250),(230,195),(140,250)]
    d.line(pts,fill=(12,10,30,255),width=42,joint='curve')
    d.line(pts,fill=(255,48,170,255),width=27,joint='curve')
    d.line(pts,fill=(40,230,232,255),width=8,joint='curve')
    d.text((118,118),'JETSETCRAFT',fill=(255,220,54,255),stroke_width=4,stroke_fill=(18,12,30,255))
    im=im.filter(ImageFilter.UnsharpMask(radius=1.0,percent=180,threshold=2)); out=GRAFF_DIR/'tag_3.png'; im.save(out)
    catalog.append({'id': 'jetsetcraft', 'texture': 'jetsetcraft:textures/graffiti/tag_3.png', 'width': 512, 'height': 320})

    # User-provided JSR graffiti archive. Originals stay untouched under source_assets;
    # runtime copies retain native dimensions/aspect ratio and only normalize PNG decode mode to RGBA.
    jsr_archive = ROOT/'source_assets/authorized/jsr/JSRGraffiti.zip'
    if not jsr_archive.exists():
        # CI/source checkouts use the public mirror, but only after byte-for-byte SHA verification against
        # the exact archive supplied by the project owner. The verified master is also mirrored to Drive.
        import hashlib, urllib.request
        cache = ROOT/'build/asset-cache/JSRGraffiti.zip'
        cache.parent.mkdir(parents=True, exist_ok=True)
        if not cache.exists():
            print('downloading pinned JSR graffiti asset pack')
            urllib.request.urlretrieve(JSR_GRAFFITI_URL, cache)
        digest = hashlib.sha256(cache.read_bytes()).hexdigest()
        if digest != JSR_GRAFFITI_SHA256:
            raise RuntimeError(f'JSRGraffiti.zip SHA-256 mismatch: {digest}')
        jsr_archive = cache
    jsr_runtime = GRAFF_DIR/'jsr'
    if jsr_archive.exists():
        import io, zipfile
        with zipfile.ZipFile(jsr_archive) as zf:
            names = sorted((n for n in zf.namelist() if n.lower().endswith('.png') and not n.endswith('/')), key=str.lower)
            for name in names:
                safe = _resource_safe_name(Path(name).name)
                out = jsr_runtime/safe
                with Image.open(io.BytesIO(zf.read(name))) as src_im:
                    rgba = src_im.convert('RGBA')
                    out.parent.mkdir(parents=True, exist_ok=True)
                    rgba.save(out, optimize=True)
                    w, h = rgba.size
                ident = 'jsr_' + _resource_safe_name(Path(name).stem).replace('.', '_')
                catalog.append({'id': ident, 'texture': f'jetsetcraft:textures/graffiti/jsr/{safe}', 'width': w, 'height': h, 'source': Path(name).name})

    manifest_dir = ROOT/'src/main/resources/assets/jetsetcraft/graffiti'
    manifest_dir.mkdir(parents=True, exist_ok=True)
    import json
    (manifest_dir/'catalog.json').write_text(json.dumps({'version': 1, 'entries': catalog}, indent=2) + '\n', encoding='utf-8')
    print('graffiti catalog entries', len(catalog))

if __name__=='__main__':
    make_textures(); make_graffiti(); make_skate(); make_quad_skate(); make_board(); make_bmx(); make_spray_can()
    for name in ('inline_skates','quad_skates','street_board','bmx','spray_can'):
        p=OBJ_DIR/f'{name}.obj'
        print(name, 'vertices', sum(1 for line in p.open() if line.startswith('v ')), 'faces', sum(1 for line in p.open() if line.startswith('f ')), 'bytes', p.stat().st_size)
