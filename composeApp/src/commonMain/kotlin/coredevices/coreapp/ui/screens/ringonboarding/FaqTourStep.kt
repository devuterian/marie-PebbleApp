package coredevices.coreapp.ui.screens.ringonboarding

import localization.localized

import CoreNav
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MobileOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coreapp.composeapp.generated.resources.Res
import coreapp.composeapp.generated.resources.index01_mic
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource

private data class FaqEntry(
    val id: String,
    val icon: ImageVector,
    val question: String,
    val answer: AnnotatedString,
    val image: DrawableResource? = null,
)

private val FaqEntries: List<FaqEntry> = listOf(
    FaqEntry(
        "bug", Icons.Default.BugReport,
        localized("What if I find a bug?"),
        ann(localized("This app is a work in progress. We're constantly experimenting and building new " +
                    "features. If you have any problems, we'd love if you report them to us with " +
                    "as much detail as possible. We'll fix it as soon as we can.\n\n" +
                    "Settings → Get Help → Report a bug (be sure to tap Index)", "아직 개발 중인 앱이라 계속 기능을 다듬고 있습니다. 문제가 생기면 상황을 자세히 알려 주십시오. 최대한 빨리 고치겠습니다.\n\n설정 → 도움받기 → 버그 제보에서 Index를 선택하십시오.")),
    ),
    FaqEntry(
        "listen", Icons.Default.Bluetooth,
        localized("Is it always listening?"),
        ann(localized("Nope! Index 01 only listens while you're holding the button. When you release, " +
                    "it'll be processed by your phone if in range.", "아닙니다! 버튼을 누르고 있을 때만 녹음합니다. 버튼을 놓으면 연결 범위 안에 있는 휴대폰에서 처리합니다.")),
    ),
    FaqEntry(
        "ask", Icons.Outlined.Lightbulb,
        localized("What can I record?"),
        ann(localized("Jot down notes, add a reminder or todo, set timers and alarms, or whatever else " +
                    "you need to remember! Double-click-and-hold to ask quick questions and get " +
                    "the answer* in a notification.\n\n*Included free for now; pricing may change.", "메모, 할 일, 미리 알림, 타이머, 알람 등 기억하고 싶은 내용을 말씀하십시오. 두 번 누르고 길게 누르면 질문할 수 있고 답은 알림으로 옵니다.\n\n현재는 무료이며 나중에 요금이 달라질 수 있습니다.")),
    ),
    FaqEntry(
        "mic", Icons.Default.Mic,
        localized("Where's the microphone?"),
        ann(localized("Look for the small hole, that's the mic.\n\n" +
                    "Hold it within 5-10 cm of your mouth when recording, and wear the ring " +
                    "so your finger doesn't cover the hole.", "작은 구멍이 마이크입니다. 녹음할 때 입에서 5~10cm 정도 떨어뜨리십시오. 손가락으로 구멍을 가리지 않도록 착용하십시오.")),
        image = Res.drawable.index01_mic,
    ),
    FaqEntry(
        "how", Icons.Default.Mic,
        localized("How do I use it?"),
        ann(localized("Hold the button, speak your mind, then release. You'll see a green light blink " +
                    "twice on your ring, then a notification in 5-10 seconds on your phone with " +
                    "the transcription.", "버튼을 누른 채 말하고 손을 떼면 됩니다. 링에서 초록 불이 두 번 깜빡인 뒤 5~10초 안에 휴대폰으로 받아 쓴 내용이 옵니다.")),
    ),
    FaqEntry(
        "offline", Icons.Default.MobileOff,
        localized("What if I want to ditch my phone?"),
        ann(localized("Index 01 can store up to 5 minutes of recordings. When you get back to your " +
                    "phone, just click the button to wake it and your recordings will automatically sync!", "Index 01에 녹음을 최대 5분까지 저장할 수 있습니다. 휴대폰 근처로 돌아와 버튼을 누르면 자동으로 동기화됩니다.")),
    ),
    FaqEntry(
        "charge", Icons.Default.BatteryChargingFull,
        localized("How do I charge it?"),
        ann(localized("You don't! Index 01 will last up to two years (or more) with 20 six second " +
                    "recordings each day.", "충전할 필요 없습니다! 하루에 6초씩 20번 녹음하면 최대 2년 이상 사용할 수 있습니다.")),
    ),
    FaqEntry(
        "offline2", Icons.Default.WifiOff,
        localized("Can it be used while offline / in bad cell service?", "인터넷이 없거나 신호가 약해도 쓸 수 있습니까?"),
        ann(localized("Yes! Your recordings are always saved to Index 01 and synced to your phone " +
                    "regardless of internet status.\n\nIf you select the optional local speech " +
                    "recognition and a local AI model, Index 01 can even process your recordings " +
                    "offline. We recommend setting it up as cloud with a local fallback for the " +
                    "best experience.", "네! 인터넷이 없어도 녹음은 Index 01에 저장되고 휴대폰과 동기화됩니다. 기기 내 음성 인식과 AI 모델을 선택하면 오프라인에서도 처리할 수 있습니다. 평소에는 클라우드를 쓰고 연결이 안 될 때 기기 내 처리를 쓰도록 설정하는 걸 권합니다.")),
    ),
)

internal val faqEntriesInitialPage = FaqEntries.lastIndex

private fun ann(s: String) = buildAnnotatedString { append(s) }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun FaqTourStep(
    initialPage: Int,
    onLeaveBackwards: () -> Unit,
    onContinue: () -> Unit,
    onExit: () -> Unit,
    coreNav: CoreNav,
) {
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { FaqEntries.size })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == FaqEntries.size - 1

    val goBack: () -> Unit = {
        if (pagerState.currentPage > 0) {
            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
        } else {
            onLeaveBackwards()
        }
    }
    BackHandler { goBack() }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBarRow(onLeading = goBack, leadingIsClose = false, onTrailingClose = onExit)

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) { page ->
            FaqTourPage(entry = FaqEntries[page], coreNav = coreNav)
        }

        // Dots
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(FaqEntries.size) { i ->
                val selected = i == pagerState.currentPage
                val dotWidth by animateDpAsState(if (selected) 22.dp else 8.dp, label = "dotW")
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(8.dp)
                        .width(dotWidth)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (selected) LocalPalette.current.primary
                            else LocalPalette.current.surfaceContainerHigh
                        ),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (pagerState.currentPage > 0) {
                OutlinedPillButton(
                    text = localized("Back"),
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    },
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                PrimaryFilledButton(
                    text = if (isLast) localized("Continue to setup") else localized("Next"),
                    onClick = {
                        if (isLast) {
                            onContinue()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun FaqTourPage(entry: FaqEntry, coreNav: CoreNav) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        // More breathing room between the top bar and the hero icon, per design.
        Spacer(Modifier.height(48.dp))
        val palette = LocalPalette.current
        if (entry.image != null) {
            // Pages with a labeled photo (e.g. mic location) get a larger square
            // tile. PNG has a white background, so we fill the pink container
            // first and draw the image with BlendMode.Multiply. Multiply needs a
            // light base or the whole photo goes dark, so always use the light
            // container color even in dark mode (MOB-8710).
            val bitmap = imageResource(entry.image)
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .aspectRatio(1f),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(color = LightPalette.primaryContainer)
                    val srcW = bitmap.width.toFloat()
                    val srcH = bitmap.height.toFloat()
                    val maxW = size.width * 0.92f
                    val maxH = size.height * 0.92f
                    val scale = minOf(maxW / srcW, maxH / srcH)
                    val drawW = srcW * scale
                    val drawH = srcH * scale
                    val offX = ((size.width - drawW) / 2f).toInt()
                    val offY = ((size.height - drawH) / 2f).toInt()
                    drawImage(
                        image = bitmap,
                        srcOffset = IntOffset.Zero,
                        srcSize = IntSize(bitmap.width, bitmap.height),
                        dstOffset = IntOffset(offX, offY),
                        dstSize = IntSize(drawW.toInt(), drawH.toInt()),
                        blendMode = BlendMode.Multiply,
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(132.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .background(palette.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = entry.icon,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = palette.primary,
                )
            }
        }
        Spacer(Modifier.height(40.dp))
        Text(
            text = entry.question,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.4).sp,
            color = LocalPalette.current.onSurface,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = entry.answer,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = LocalPalette.current.onSurfaceVariant,
        )
        if (entry.id == "how") {
            Spacer(Modifier.height(20.dp))
            RingDemo(nav = coreNav)
        }
        Spacer(Modifier.height(24.dp))
    }
}