package com.peaknote.demo.service;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MeetingSummaryServiceTest {

    @Autowired
    private MeetingSummaryService meetingSummaryService;

    @Test
    void generateSummary_realModel_writesResult() throws Exception {
        Instant startTime = Instant.parse("2025-01-01T00:00:00Z");
        String transcript = """
                WEBVTT

                4bb75105-a44d-4f9d-87b6-fc3ac9a05817/36-0
                00:00:04.085 --> 00:00:08.857
                <v administrator>So what do you think about the two
                centuries from Washington Sundar and</v>

                4bb75105-a44d-4f9d-87b6-fc3ac9a05817/36-1
                00:00:08.857 --> 00:00:13.961
                <v administrator>Ravindra Jadeja? Unbelievable test,
                especially with with just like the being</v>

                4bb75105-a44d-4f9d-87b6-fc3ac9a05817/36-2
                00:00:13.961 --> 00:00:19.065
                <v administrator>it just like the day five of a Test match.
                Considering that there was like 2</v>

                4bb75105-a44d-4f9d-87b6-fc3ac9a05817/36-3
                00:00:19.065 --> 00:00:20.325
                <v administrator>sessions remaining.</v>

                4bb75105-a44d-4f9d-87b6-fc3ac9a05817/70-0
                00:00:21.085 --> 00:00:25.634
                <v administrator>Washington to unreal like I really did,
                break England's backs.</v>

                4bb75105-a44d-4f9d-87b6-fc3ac9a05817/70-1
                00:00:25.634 --> 00:00:30.617
                <v administrator>Particularly being from 042.
                There was a good real guard action from</v>

                4bb75105-a44d-4f9d-87b6-fc3ac9a05817/70-2
                00:00:30.617 --> 00:00:35.745
                <v administrator>Rahul and Gil and real yeah.
                And then like the drop catch of Jadeja is</v>

                4bb75105-a44d-4f9d-87b6-fc3ac9a05817/70-3
                00:00:35.745 --> 00:00:37.045
                <v administrator>the turning point.</v>

                4bb75105-a44d-4f9d-87b6-fc3ac9a05817/102-0
                00:00:37.845 --> 00:00:42.492
                <v administrator>Yeah, that was the one.
                And also like Gil's catch on day 4/4 that</v>

                4bb75105-a44d-4f9d-87b6-fc3ac9a05817/102-1
                00:00:42.492 --> 00:00:47.070
                <v administrator>those two were really good.
                But also considering that, you know,</v>

                4bb75105-a44d-4f9d-87b6-fc3ac9a05817/102-2
                00:00:47.070 --> 00:00:50.591
                <v administrator>but still you they still have the bat
                after that,</v>

                4bb75105-a44d-4f9d-87b6-fc3ac9a05817/102-3
                00:00:50.591 --> 00:00:54.605
                <v administrator>even though you can give you can give
                like like chances.</v>

                4bb75105-a44d-4f9d-87b6-fc3ac9a05817/152-0
                00:00:54.845 --> 00:00:59.295
                <v administrator>But it was India still had to bat and do
                do you think the 5th test we have a</v>

                4bb75105-a44d-4f9d-87b6-fc3ac9a05817/152-1
                00:00:59.295 --> 00:01:02.762
                <v administrator>chance to square the series to all? Yeah,
                100% I think. OK,</v>

                4bb75105-a44d-4f9d-87b6-fc3ac9a05817/152-2
                00:01:02.762 --> 00:01:06.807
                <v administrator>I think was it this would have definitely
                burnt out the England Bowl,</v>

                4bb75105-a44d-4f9d-87b6-fc3ac9a05817/152-3
                00:01:06.807 --> 00:01:11.257
                <v administrator>England bowlers and you could see that
                Archer was not was a pulling up well,</v>

                4bb75105-a44d-4f9d-87b6-fc3ac9a05817/152-4
                00:01:11.257 --> 00:01:14.725
                <v administrator>like Ben Stokes did not bowl on day four.
                So there I think.</v>

                4bb75105-a44d-4f9d-87b6-fc3ac9a05817/191-0
                00:01:14.805 --> 00:01:19.277
                <v administrator>We have a lot of injury concerns and
                especially being at like a quick turn</v>

                4bb75105-a44d-4f9d-87b6-fc3ac9a05817/191-1
                00:01:19.277 --> 00:01:22.139
                <v administrator>around from this test to the 5th test.
                I think.</v>

                4bb75105-a44d-4f9d-87b6-fc3ac9a05817/191-2
                00:01:22.139 --> 00:01:26.969
                <v administrator>I think India have a really good chance.
                So then like I will have an action item</v>

                4bb75105-a44d-4f9d-87b6-fc3ac9a05817/191-3
                00:01:26.969 --> 00:01:30.905
                <v administrator>on you to just get the tickets for the
                oral test on the 5th. Yep,</v>

                4bb75105-a44d-4f9d-87b6-fc3ac9a05817/191-4
                00:01:30.905 --> 00:01:34.125
                <v administrator>I'll get the tickets for you. Yeah. Yeah.
                Good, good.</v>
                """;

        String result = meetingSummaryService.generateSummary(startTime, transcript);

        Path outDir = Paths.get("target", "ai");
        Files.createDirectories(outDir);
        Path outFile = outDir.resolve("MeetingSummaryService.result.txt");
        Files.writeString(outFile, result, StandardCharsets.UTF_8);
    }
}

