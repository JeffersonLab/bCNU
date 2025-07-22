package cnuphys.fastMCed.consumers;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jlab.clas.physics.Particle;
import org.jlab.clas.physics.PhysicsEvent;

import cnuphys.fastMCed.fastmc.ParticleHits;
import cnuphys.fastMCed.snr.SNRManager;
import cnuphys.fastMCed.streaming.StreamProcessStatus;
import cnuphys.fastMCed.streaming.StreamReason;
import cnuphys.snr.NoiseReductionParameters;

public class BinaryFileMLTestData extends ASNRConsumer {

    private DataOutputStream _binaryOut;
	private static long count = 0;


    @Override
    public String getConsumerName() {
        return "Binary File for ML Test Data";
    }

    @Override
    public void streamingChange(StreamReason reason) {

        System.out.println("BinaryFileMLTestData streaming change reason: [" + reason.name() + "]");

        if (reason == StreamReason.STARTED) {
            System.out.println("Stream Started");

            String homeDir = System.getProperty("user.home");
            File dir = new File(homeDir, "testdata");
            dir.mkdirs();

            File file = getNextBinaryFile(dir);
            try {
                _binaryOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (reason == StreamReason.STOPPED) {
            System.out.println("Stream Stopped");
            try {
                if (_binaryOut != null) {
                    _binaryOut.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File getNextBinaryFile(File dir) {
        int maxIndex = 0;
        Pattern pattern = Pattern.compile("snrML_(\\d+)\\.dat");
        File[] files = dir.listFiles((d, name) -> name.startsWith("snrML_") && name.endsWith(".dat"));

        if (files != null) {
            for (File file : files) {
                Matcher matcher = pattern.matcher(file.getName());
                if (matcher.matches()) {
                    int idx = Integer.parseInt(matcher.group(1));
                    if (idx > maxIndex) {
                        maxIndex = idx;
                    }
                }
            }
        }

        return new File(dir, String.format("snrML_%d.dat", maxIndex + 1));
    }

    @Override
    public StreamProcessStatus streamingPhysicsEvent(PhysicsEvent event, List<ParticleHits> particleHits) {

    	try {
    	
        int numParticles = event.count();
        _binaryOut.writeInt(numParticles);
		System.out.println("number of particles: " + numParticles);
		for (int i = 0; i < numParticles; i++) {
			Particle particle = event.getParticle(i);
			_binaryOut.writeInt(particle.charge());
			_binaryOut.writeDouble(particle.vertex().x());
			_binaryOut.writeDouble(particle.vertex().y());
			_binaryOut.writeDouble(particle.vertex().z());
			_binaryOut.writeDouble(1000 * particle.p()); // convert to mev
			_binaryOut.writeDouble(Math.toDegrees(particle.theta()));
			_binaryOut.writeDouble(Math.toDegrees(particle.phi()));
		} // end of particle loop
		
		// now the snr results
		// data arrays filled left/right superlayers 1..6
		byte[][] snrData = new byte[12][112];

		// For now use only sector 1
		int sector = 1;
		int index = 0;
        for (int superLayer = 1; superLayer <= 6; superLayer++) {
			NoiseReductionParameters parameters = SNRManager.getInstance().getParameters(sector - 1,
					superLayer - 1);

			// left
			for (int wire = 0; wire < 112; wire++) {
				boolean leftSeg = parameters.getLeftSegments().checkBit(wire);
				if (leftSeg) {
					int numMiss = parameters.missingLayersUsed(NoiseReductionParameters.LEFT_LEAN, wire);
					snrData[index][wire] = (byte) (3 - numMiss);
				}
			}
			index++;
			// right
			for (int wire = 0; wire < 112; wire++) {
				boolean rightSeg = parameters.getRightSegments().checkBit(wire);
				if (rightSeg) {
					int numMiss = parameters.missingLayersUsed(NoiseReductionParameters.RIGHT_LEAN, wire);
					snrData[index][wire] = (byte) (3 - numMiss);
				}
			}

			index++;
      } // end of superlayer loop
    	
		for (byte[] row : snrData) {
			_binaryOut.write(row);
		}
		_binaryOut.flush();
    	}
    	catch (IOException e) {
    		e.printStackTrace();

    	}

        System.out.println("BinaryFileMLTestData: " + (++count));
 		return null;

 //       return StreamProcessStatus.CONTINUE;
    }

    @Override
    public void newPhysicsEvent(PhysicsEvent event, List<ParticleHits> particleHits) {
        // no-op for streaming binary writer
    }
}
