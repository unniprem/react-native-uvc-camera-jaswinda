import React from 'react';
import {
  NativeModules,
  findNodeHandle,
  requireNativeComponent,
  NativeEventEmitter,
  type NativeMethods,
  Platform,
} from 'react-native';
import type { UVCCameraProps } from './UVCCameraProps';
import type { PhotoFile } from './PhotoFile';

const CameraModule = NativeModules.UVCCameraView;
const eventEmitter = new NativeEventEmitter(CameraModule);

if (CameraModule == null) {
  console.error("Camera: Native Module 'UVCCameraView' was null!");
}

const ComponentName = 'UVCCameraView';

type NativeUVCCameraViewProps = UVCCameraProps;
const NativeUVCCameraView = requireNativeComponent<NativeUVCCameraViewProps>(ComponentName);

type RefType = React.Component<NativeUVCCameraViewProps> & Readonly<NativeMethods>;

interface CameraError extends Error {
  code: string;
  message: string;
}

export class UVCCamera extends React.PureComponent<UVCCameraProps> {
  private readonly ref: React.RefObject<RefType>;
  private eventSubscriptions: any[] = [];
  private isInitialized: boolean = false;
  private reconnectTimeout: NodeJS.Timeout | null = null;
  private reconnectAttempts: number = 0;
  private readonly MAX_RECONNECT_ATTEMPTS = 3;
  private readonly RECONNECT_DELAY = 1000;

  constructor(props: UVCCameraProps) {
    super(props);
    this.ref = React.createRef<RefType>();
    this.state = {
      isConnected: false,
      error: null,
    };
  }

  private get handle(): number | null {
    return findNodeHandle(this.ref.current);
  }

  componentDidMount() {
    this.setupEventListeners();
    this.initializeCamera();
  }

  componentWillUnmount() {
    this.cleanup();
  }

  private setupEventListeners(): void {
    this.eventSubscriptions = [
      eventEmitter.addListener('onCameraConnected', this.handleCameraConnected),
      eventEmitter.addListener('onCameraDisconnected', this.handleCameraDisconnected),
      eventEmitter.addListener('onCameraError', this.handleCameraError)
    ];
  }

  private async initializeCamera(): Promise<void> {
    try {
      await this.openCamera();
      this.isInitialized = true;
      this.reconnectAttempts = 0;
    } catch (error) {
      this.handleCameraError(error as CameraError);
    }
  }

  private handleCameraConnected = (): void => {
    this.setState({ isConnected: true, error: null });
    this.reconnectAttempts = 0;
    if (this.props.onCameraConnected) {
      this.props.onCameraConnected();
    }
  };

  private handleCameraDisconnected = (): void => {
    this.setState({ isConnected: false });
    if (this.props.onCameraDisconnected) {
      this.props.onCameraDisconnected();
    }
    this.handleReconnection();
  };

  private handleCameraError = (error: CameraError): void => {
    this.setState({ error: error.message });
    if (this.props.onCameraError) {
      this.props.onCameraError(error);
    }
    this.handleReconnection();
  };

  private handleReconnection = (): void => {
    if (this.reconnectAttempts < this.MAX_RECONNECT_ATTEMPTS) {
      this.reconnectAttempts++;
      if (this.reconnectTimeout) {
        clearTimeout(this.reconnectTimeout);
      }
      
      this.reconnectTimeout = setTimeout(async () => {
        try {
          await this.openCamera();
        } catch (error) {
          this.handleCameraError(error as CameraError);
        }
      }, this.RECONNECT_DELAY * this.reconnectAttempts);
    }
  };

  private async cleanup(): Promise<void> {
    this.eventSubscriptions.forEach(subscription => subscription.remove());
    if (this.reconnectTimeout) {
      clearTimeout(this.reconnectTimeout);
    }
    if (this.isInitialized) {
      try {
        await this.closeCamera();
      } catch (error) {
        console.error('Error during camera cleanup:', error);
      }
    }
  }

  // Existing methods with error handling
  public async openCamera(): Promise<void> {
    try {
      await CameraModule.openCamera(this.handle);
    } catch (error) {
      throw this.formatError(error);
    }
  }

  public async closeCamera(): Promise<void> {
    try {
      await CameraModule.closeCamera(this.handle);
    } catch (error) {
      throw this.formatError(error);
    }
  }

  public async takePhoto(): Promise<PhotoFile> {
    try {
      return await CameraModule.takePhoto(this.handle);
    } catch (error) {
      throw this.formatError(error);
    }
  }

  public async updateAspectRatio(width: number, height: number): Promise<void> {
    try {
      await CameraModule.updateAspectRatio(this.handle, width, height);
    } catch (error) {
      throw this.formatError(error);
    }
  }

  public async setCameraBright(value: number): Promise<void> {
    try {
      await CameraModule.setCameraBright(this.handle, value);
    } catch (error) {
      throw this.formatError(error);
    }
  }

  public async setCameraContrast(value: number): Promise<void> {
    try {
      await CameraModule.setContast(this.handle, value);
    } catch (error) {
      throw this.formatError(error);
    }
  }

  public async setCameraSaturation(value: number): Promise<void> {
    try {
      await CameraModule.setSaturation(this.handle, value);
    } catch (error) {
      throw this.formatError(error);
    }
  }

  public async setCameraSharpness(value: number): Promise<void> {
    try {
      await CameraModule.setSharpness(this.handle, value);
    } catch (error) {
      throw this.formatError(error);
    }
  }

  public async setCameraZoom(value: number): Promise<void> {
    try {
      await CameraModule.setZoom(this.handle, value);
    } catch (error) {
      throw this.formatError(error);
    }
  }

  private formatError(error: any): CameraError {
    return {
      code: error.code || 'UNKNOWN_ERROR',
      message: error.message || 'An unknown error occurred',
      ...error,
    };
  }

  public render(): React.ReactNode {
    return <NativeUVCCameraView {...this.props} ref={this.ref} />;
  }
}