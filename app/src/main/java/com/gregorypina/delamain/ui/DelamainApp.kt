package com.gregorypina.delamain.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.gregorypina.delamain.R
import com.gregorypina.delamain.domain.*
import kotlinx.coroutines.delay
import kotlin.math.*

private val Background=Color(0xFF14181D);private val Cyan=Color(0xFF2E8BFF);private val ErrorRed=Color(0xFFFF3B5C)
private fun imageResFor(s:InteractionFace)=when(s){InteractionFace.BOOT,InteractionFace.IDLE->R.drawable.face_idle;InteractionFace.LISTENING->R.drawable.face_listening;InteractionFace.THINKING->R.drawable.face_thinking;InteractionFace.SPEAKING->R.drawable.face_speaking;InteractionFace.ERROR->R.drawable.face_error}
private fun intensity(s:InteractionFace)=when(s){InteractionFace.BOOT->.55f;InteractionFace.IDLE->.05f;InteractionFace.LISTENING->.12f;InteractionFace.THINKING->.55f;InteractionFace.SPEAKING->.2f;InteractionFace.ERROR->.9f}
@Composable fun DelamainApp(){MaterialTheme{Surface(color=Background,modifier=Modifier.fillMaxSize()){var snapshot by remember{mutableStateOf(InteractionSnapshot())};val coordinator=remember{InteractionCoordinator{snapshot=it}}
 LaunchedEffect(coordinator){delay(2200);coordinator.bootFinished()};DisposableEffect(coordinator){onDispose{coordinator.shutdown()}}
 Box(Modifier.fillMaxSize()){DelamainScreen(snapshot);DebugCommandPanel(interactionCoordinator=coordinator)}}}}
@Composable private fun DelamainScreen(snapshot:InteractionSnapshot){val state=snapshot.face;val color=if(state==InteractionFace.ERROR)ErrorRed else Cyan
 Box(Modifier.fillMaxSize().background(Background)){Crossfade(state,label="face-crossfade"){GlitchFace(imageResFor(it),intensity(it),if(it==InteractionFace.ERROR)ErrorRed else Cyan,Modifier.fillMaxSize())};Text(snapshot.hud,Modifier.align(Alignment.BottomStart).padding(start=28.dp,bottom=20.dp),style=TextStyle(color=color.copy(alpha=.85f),fontSize=11.sp,fontFamily=FontFamily.Monospace,fontWeight=FontWeight.Medium,letterSpacing=2.sp));Text("${stringResource(R.string.app_name)} // V0.1",Modifier.align(Alignment.TopEnd).padding(end=28.dp,top=20.dp),style=TextStyle(color=color.copy(alpha=.35f),fontSize=10.sp,fontFamily=FontFamily.Monospace,letterSpacing=1.5.sp))}}
@Composable private fun GlitchFace(imageRes:Int,intensity:Float,tint:Color,modifier:Modifier=Modifier){val image=ImageBitmap.imageResource(imageRes);val transition=rememberInfiniteTransition(label="glitch");val phase by transition.animateFloat(0f,1f,infiniteRepeatable(tween(1800),RepeatMode.Restart),label="phase");val flicker by transition.animateFloat(0f,1f,infiniteRepeatable(tween(90),RepeatMode.Reverse),label="flicker")
 Canvas(modifier){val scale=minOf(size.width/image.width,size.height/image.height);val w=image.width*scale;val h=image.height*scale;val x=(size.width-w)/2;val y=(size.height-h)/2;val count=28;val sh=image.height/count
  for(i in 0 until count){val sy=i*sh;val srcH=if(i==count-1)image.height-sy else sh;val dy=y+sy.toFloat()/image.height*h;val dh=srcH.toFloat()/image.height*h;val trigger=pseudoRandom(i,phase);val shift=if(trigger>1f-intensity*.55f)(pseudoRandom(i+500,phase)-.5f)*w*.10f*intensity else 0f;drawImage(image,IntOffset(0,sy),IntSize(image.width,srcH),IntOffset((x+shift).toInt(),dy.toInt()),IntSize(w.toInt(),maxOf(1,dh.toInt())));if(shift!=0f){drawRect(tint.copy(alpha=.08f*intensity),Offset(0f,dy),Size(size.width,maxOf(1f,dh)))}}
  var line=(phase*15f)%5f;while(line<size.height){drawLine(tint.copy(alpha=.03f+.05f*intensity),Offset(0f,line),Offset(size.width,line),1f);line+=5f};drawRect(Color.White.copy(alpha=.02f+.10f*intensity*abs(sin(flicker*Math.PI.toFloat()+phase*30f))));if(intensity>.6f&&pseudoRandom(999,phase)>.88f){val yy=pseudoRandom(1000,phase)*size.height;drawRect(Color.Black.copy(alpha=.5f),Offset(0f,yy),Size(size.width,size.height*.01f+3f))}}}
private fun pseudoRandom(seed:Int,phase:Float):Float{val x=sin(seed*12.9898f+phase*78.233f)*43758.5453f;return x-floor(x)}
