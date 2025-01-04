import type { ViewProps } from 'react-native';

export interface UVCCameraProps {
  style?: any;
  onCameraConnected?: () => void;
  onCameraDisconnected?: () => void;
  onCameraError?: (error: Error) => void;
  onFrame?: (frame: any) => void;
  // Add any additional props needed for your implementation
}
