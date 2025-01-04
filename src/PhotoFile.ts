import type { TemporaryFile } from './TemporaryFile';

export interface PhotoFile {
  uri: string;
  width: number;
  height: number;
  size?: number;
  // Add any additional photo metadata properties
}
